package net.north101.android.ghplayertracker

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import net.north101.android.ghplayertracker.data.Character
import net.north101.android.ghplayertracker.data.CharacterClass
import net.north101.android.ghplayertracker.data.CharacterClassData
import net.north101.android.ghplayertracker.data.CharacterData

class ClassModel(
    application: Application
) : AndroidViewModel(application) {
    private val TAG = ClassModel::class.java.simpleName

    val classList = ClassListLiveData(this)
    val characterList = CharacterListLiveData(this)

    val context
        get() = this.getApplication<Application>()
}

enum class LiveDataState {
    INIT,
    LOADING,
    FINISHED
}

class ClassListLiveData(
    private val model: ClassModel
) : MutableLiveData<ArrayList<CharacterClass>>() {
    private val TAG = ClassListLiveData::class.java.simpleName

    val state = MutableLiveData<LiveDataState>()
    var task: ClassListTask? = null

    init {
        state.value = LiveDataState.INIT
    }

    fun load(): ClassListTask {
        var task = this.task
        if (task != null && !task.isCancelled)
            return task

        task = ClassListTask(this.model, LiveDataState.LOADING)
        task.execute()
        this.task = task

        return task
    }
}

class ClassListTask(
    private val model: ClassModel,
    private val state: LiveDataState
) : AsyncTask<Void, Void, ArrayList<CharacterClass>>() {
    private val TAG = ClassListTask::class.java.simpleName
    override fun onPreExecute() {
        model.classList.state.value = state
    }

    override fun doInBackground(vararg voids: Void): ArrayList<CharacterClass> {
        return CharacterClassData.load(model.context).toList()
    }

    override fun onPostExecute(data: ArrayList<CharacterClass>) {
        model.classList.value = data
        model.classList.state.value = LiveDataState.FINISHED
        model.classList.task = null
    }
}

class CharacterListLiveData(
    private val model: ClassModel
) : MutableLiveData<List<Character>>() {
    private val TAG = CharacterListLiveData::class.java.simpleName

    val state = MutableLiveData<LiveDataState>()
    var task: CharacterListLoadTask? = null

    init {
        state.value = LiveDataState.INIT
    }

    fun load(): CharacterListLoadTask {
        var task = this.task
        if (task != null && !task.isCancelled)
            return task

        task = CharacterListLoadTask(this.model, LiveDataState.LOADING)
        task.execute()
        this.task = task

        return task
    }

    fun set(value: ArrayList<Character>) {
        this.value = value
    }
}

class CharacterListLoadTask(
    private val model: ClassModel,
    private val state: LiveDataState
) : AsyncTask<Void, Void, Pair<ArrayList<CharacterClass>, ArrayList<Character>>>() {
    private val TAG = CharacterListLoadTask::class.java.simpleName

    override fun onPreExecute() {
        model.characterList.state.value = state
        if (model.classList.value == null) {
            model.classList.state.value = state
        }
    }

    override fun doInBackground(vararg voids: Void): Pair<ArrayList<CharacterClass>, ArrayList<Character>> {
        var classList = model.classList.value
        if (classList == null) {
            classList = CharacterClassData.load(model.context).toList()
        }
        val characterList = CharacterData.load(model.context).toList(classList)
        return Pair(classList, characterList)
    }

    override fun onPostExecute(data: Pair<ArrayList<CharacterClass>, ArrayList<Character>>) {
        model.classList.value = data.first
        model.classList.state.value = LiveDataState.FINISHED
        model.characterList.value = data.second
        model.characterList.state.value = LiveDataState.FINISHED

        model.characterList.task = null
    }
}