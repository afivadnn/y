package com.afivadnan.y

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.afivadnan.y.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: FriendViewModel
    private lateinit var adapter: RvAdapter
    private val list = ArrayList<Friend>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        enableEdgeToEdge()
        setContentView(binding.root)

        val factoryModel = Factory(this)
        viewModel = ViewModelProvider(this, factoryModel)[FriendViewModel::class.java]

        adapter = RvAdapter { _, data ->
            val intent = Intent(this, AddFriend::class.java).apply {
                putExtra("FRIEND_ID", data.id)
            }
            startActivity(intent)
        }
        binding.TvShow.adapter = adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getFriend().collect { friends ->
                        list.clear()
                        list.addAll(friends)
                        adapter.setData(list)
                    }
                }
            }
        }
        val add: FloatingActionButton = findViewById(R.id.bf_add)
        add.setOnClickListener {
            val intent = Intent(this, AddFriend::class.java)
            startActivity(intent)
        }
    }
}
