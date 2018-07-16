package com.merlinjobs.currencyexchange.exchange

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import com.merlinjobs.currencyexchange.BaseApplication
import com.merlinjobs.currencyexchange.R
import com.merlinjobs.currencyexchange.Utils.DecimalDigitsInputFilter
import com.merlinjobs.currencyexchange.data.PREFERENCE_DIALOG_TAG
import com.merlinjobs.currencyexchange.data.local.models.Currency
import com.merlinjobs.currencyexchange.data.local.models.ExchangeConversion
import com.merlinjobs.currencyexchange.preferences.PreferenceDialogFragment
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_exchange.*
import kotlinx.android.synthetic.main.fragment_preference.*

class ExchangeActivity : AppCompatActivity(), IExchangeActivityView {


    private val mPresenter: IExchangeActivityPresenter = ExchangeActivityPresenter()
    private val viewModel: ExchangeActivityViewModel = ExchangeActivityViewModel()

    private lateinit var mMap: Map<String, Currency>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange)
        lifecycle.addObserver(mPresenter)
        mPresenter.bind(this)
        initComponents()
    }

    override fun initComponents() {
        mRVExchanges?.setHasFixedSize(true)
        this?.let {
            mRVExchanges?.layoutManager = LinearLayoutManager(it)
        }
        mETValue?.filters = arrayOf(DecimalDigitsInputFilter(1))
        Single.create<Map<String, Currency>> { emitter ->
            val map = HashMap<String, Currency>()
            BaseApplication.getInstance().getmCurrencies().forEach {
                map[it.code] = it
            }
            emitter.onSuccess(map)
        }.subscribe(Consumer<Map<String, Currency>> {
            mMap = it
            mRVExchanges?.adapter = ExchangeRVAdapter(it)
        })

        mETValue.setOnKeyListener { view, keyCode, keyEvent ->
            when {
                (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) -> {
                    val disposable = object : DisposableObserver<List<ExchangeConversion>>() {
                        override fun onComplete() {

                        }

                        override fun onNext(t: List<ExchangeConversion>) {
                            showConversions(t)
                        }

                        override fun onError(e: Throwable) {
                            viewModel.removeDisposable(this)
                        }
                    }
                    viewModel.addAndExecuteDisposable(disposable)
                    if (viewModel.isNumeric(mETValue.text.toString())) {
                        viewModel.calculateExchange(mETValue.text.toString().toDouble())
                    } else {
                        clearConversions()
                    }
                    mETValue.hideKeyboard()
                    true
                }
            }
            false
        }
    }


    override fun showProgressBar() {
    }

    override fun hideProgressBar() {
    }

    override fun showConversions(conversions: List<ExchangeConversion>) {
        mRVExchanges?.adapter?.let {
            (it as ExchangeRVAdapter).clear()
            (it as ExchangeRVAdapter).setCurrencies(mMap)
            (it as ExchangeRVAdapter).addConversions(conversions)

        }
    }

    override fun showPreferenceDialog() {
        val ft = fragmentManager.beginTransaction()
        val prev = fragmentManager.findFragmentByTag(PREFERENCE_DIALOG_TAG)
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // Create and show the dialog.
        val newFragment = PreferenceDialogFragment.newInstance()
        newFragment.show(ft, PREFERENCE_DIALOG_TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        item?.let {
            if (item.itemId == R.id.action_favorite_currencies) {
                showPreferenceDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            menuInflater.inflate(R.menu.exchange_menu, it)

        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun clearConversions() {
        mRVExchanges?.adapter?.let {
            (it as ExchangeRVAdapter).clear()
        }
    }

    override fun getValueEditText(): EditText? {
        return mETValue
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
