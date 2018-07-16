package com.merlinjobs.currencyexchange.exchange

import android.widget.EditText
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import com.merlinjobs.currencyexchange.BaseApplication
import com.merlinjobs.currencyexchange.core.use_cases.base.ICompletableUseCase
import com.merlinjobs.currencyexchange.core.use_cases.base.IObservableUseCase
import com.merlinjobs.currencyexchange.core.use_cases.base.ISingleUseCase
import com.merlinjobs.currencyexchange.data.local.models.ExchangeConversion
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Daniel on 7/16/18.
 */
class ExchangeActivityViewModel {

    @Inject
    lateinit var mSubscribeToExchangeConversionsUseCase: IObservableUseCase<List<ExchangeConversion>, Any?>

    @Inject
    lateinit var mCalculateExchangeRateUseCase: ICompletableUseCase<Double>

    private val mDisposableBag = CompositeDisposable()

    init {
        BaseApplication.getInstance()
                .useCaseComponent.inject(this)

    }

    fun addAndExecuteDisposable(disposable: DisposableObserver<List<ExchangeConversion>>) {
        mDisposableBag.add(disposable)
        mSubscribeToExchangeConversionsUseCase.execute(null, disposable)
    }

    fun removeDisposable(disposable: DisposableObserver<List<ExchangeConversion>>) {
        mDisposableBag.remove(disposable)
    }

    fun calculateExchange(value: Double) {
        val disposable = object : DisposableCompletableObserver() {
            override fun onComplete() {
                mDisposableBag.remove(this)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                mDisposableBag.remove(this)
            }
        }

        mDisposableBag.add(disposable)
        mCalculateExchangeRateUseCase.execute(value, disposable)

    }

    fun isNumeric(str: String): Boolean {
        try {
            str.toDouble()
        } catch (e: NumberFormatException) {
            return false
        }

        return true
    }
}