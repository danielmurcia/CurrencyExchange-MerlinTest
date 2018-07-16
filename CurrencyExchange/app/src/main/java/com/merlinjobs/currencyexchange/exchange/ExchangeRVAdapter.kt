package com.merlinjobs.currencyexchange.exchange

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

import com.merlinjobs.currencyexchange.R
import com.merlinjobs.currencyexchange.data.local.models.Currency
import com.merlinjobs.currencyexchange.data.local.models.ExchangeConversion


class ExchangeRVAdapter(mCurrencies: Map<String, Currency>) : RecyclerView.Adapter<ExchangeViewHolder>() {


    private lateinit var mCurrencies: Map<String, Currency>

    private var mConversions = ArrayList<ExchangeConversion>()


    init {
        sortData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.exchange_item, parent, false)
        return ExchangeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExchangeViewHolder, position: Int) {
        if (mCurrencies.containsKey(mConversions[position].currency)) {
            holder.let {
                holder.mTVCurrencyAbbreviation.text = mCurrencies[mConversions[position].currency]?.code

                holder.mTVCurrencyName.text = mCurrencies[mConversions[position].currency]?.name

                holder.mTVCurrencySymbol.text = mCurrencies[mConversions[position].currency]?.symbol

                holder.mTVValue.text = holder.mTVCurrencySymbol.text

                holder.mTVValue.append(String.format(Locale.getDefault(),
                        "%1$,.2f",
                        mConversions[position].value))
            }
        }


    }

    override fun getItemCount(): Int {
        return mConversions.size
    }

    fun setCurrencies(currencies: Map<String, Currency>) {
        mCurrencies = currencies
    }

    fun addConversions(conversions: List<ExchangeConversion>) {
        mConversions.addAll(conversions)
        sortData()
        notifyItemRangeChanged(0, mConversions.size - 1);

    }

    fun clear() {
        mConversions.clear()
        notifyDataSetChanged()
        sortData()
    }

    fun sortData() {
        mConversions = ArrayList(mConversions.sortedBy {
            it.value
        }.reversed())
    }
}
