package com.example.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.launch

class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {

    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    private fun getNewItemEntry(itemName: String, itemPrice: String, itemCount: String): Item {
        return Item(
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt()
        )
    }

    fun addNewItem(itemName: String, itemPrice: String, itemCount: String) {
        val newItem = getNewItemEntry(itemName, itemPrice, itemCount)
        insertItem(newItem)
    }

    private fun getUpdatedItemEntry(
        id: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String
    ): Item {
        val price = itemPrice.replace(',', '.')
        return Item(id, itemName, price.toDouble(), itemCount.toInt())
    }

    fun updateItem(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String
    ) {
        val updateItem = getUpdatedItemEntry(itemId, itemName, itemPrice, itemCount)
        updateItem(updateItem)
    }

    fun isEntryValid(itemName: String, itemPrice: String, itemCount: String): Boolean {
        return !(itemName.isBlank() || itemPrice.isBlank() || itemCount.isBlank())
    }

    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    fun sellItem(item: Item) {
        if (item.quantityInStock > 0) {
            val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
            updateItem(newItem)
        }
    }

    fun isStockAvailable(item: Item): Boolean {
        return item.quantityInStock > 0
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }
}

class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            return InventoryViewModel(itemDao) as T
        } else throw IllegalStateException("Unknown ViewModel class")
    }
}