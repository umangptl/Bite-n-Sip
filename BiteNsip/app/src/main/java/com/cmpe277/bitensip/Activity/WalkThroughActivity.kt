package com.cmpe277.bitensip

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.cmpe277.bitensip.databinding.ActivityWalkthroughBinding

class WalkThroughActivity : AppCompatActivity() {
    private var viewPagerAdapter: WalkThroughAdapter? = null
    private lateinit var binding: ActivityWalkthroughBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = ActivityWalkthroughBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up ViewPager adapter
        val viewPagerAdapter = WalkThroughAdapter(supportFragmentManager)
        binding.walkThroughPager.adapter = viewPagerAdapter

        // Set up DotsIndicator with ViewPager
        binding.indicator.setViewPager(binding.walkThroughPager)
    }


    override fun onStart() {
        super.onStart()
        binding.getStarted.setOnClickListener {
            startActivity(Intent(this, InitUserInfoActivity::class.java))
            finish()
        }
    }

    private inner class WalkThroughAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return 3
        }

        override fun getItem(i: Int): Fragment {
            return when (i) {
                0 -> WalkThroughOne()
                1 -> WalkThroughTwo()
                2 -> WalkThroughThree()
                else -> WalkThroughOne()
            }

        }
    }

    class WalkThroughOne : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.walk_through_one, container, false)
        }
    }

    class WalkThroughTwo : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.walk_through_two, container, false)
        }
    }

    class WalkThroughThree : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.walk_through_three, container, false)
        }
    }

}