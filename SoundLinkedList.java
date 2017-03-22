import java.util.Iterator;

public class SoundLinkedList implements SoundList {

	private Link head;
	private Link tail;
	private float sampleRate;
	private int numChannels;
	private int numSamples;

	public SoundLinkedList(float sampleRate, int numChannels) {

		this.sampleRate = sampleRate;
		this.numChannels = numChannels;
		head = tail = null;

	}

	/**
	 * The number of channels in the SoundList
	 *
	 * @return The number of channels in the SoundList
	 */
	@Override
	public int getNumChannels() {

		return numChannels;
	}

	/**
	 * Returns the sample rate, in samples per second
	 *
	 * @return The sample rate, in samples per second
	 */
	@Override
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * Returns the number of samples in the MusicList
	 *
	 * @return The number of samples in the MusicList.
	 */
	@Override
	public int getNumSamples() {
		return this.numSamples / numChannels;
	}

	/**
	 * Returns The duration of the sound, in seconds.
	 *
	 * @return the duration of the sound, in seconds.
	 */
	@Override
	public float getDuration() {
		return this.getNumSamples() / sampleRate;
	}

	/**
	 * Fade in. Smoothly ramp the volume up for fadeDuration sections.
	 *
	 * @param fadeDuration
	 *            The time (in seconds) to fade in.
	 */
	@Override
	public void fadeIn(float fadeDuration) {
		Link curr = head;
		Link last = tail;

		float length = (1 / this.getSampleRate());

	}

	/**
	 * Fade out. Smoothly ramp the volume down for fadeDuration sections.
	 *
	 * @param fadeDuration
	 *            The time (in seconds) to fade in.
	 */
	@Override
	public void fadeOut(float fadeDuration, float startTime) {
		// TODO Auto-generated method stub

	}

	/**
	 * Remove the silence at the front and back of the clip. All samples at the
	 * beginning of the clip whose absolute value is less than or equal to
	 * maxLevel are removed, until a sample whose absolute value is that is >
	 * maxLevel appears. Then retain all samples until the end, at which point
	 * all samples whose absolute value is <= maxLevel are removed. Note that a
	 * value > maxLevel on any channel is sufficient to keep the sample.
	 *
	 * @param maxLevel
	 *            The level at which the sample is kept.
	 */
	public void trimSilence(float maxLevel) {

		Link previous = null;
		Link temp = head;
		boolean skip = true;

		while (temp.nextSample() != null) {
			if (temp.nextSample().data == maxLevel) {
				previous = temp;
				if (temp.nextSample() == null) {
					tail = previous;
				}
				previous.setNextSample(previous.nextSample().nextSample());
				skip = false;
			}
			if (temp.nextSample() != null && skip) {
				temp = temp.nextSample();
			}
			skip = true;

		}

	}

	// helper method for reversing
	private void swap(Link l1, Link l2) {
		Link t1 = l1;
		Link t2 = l2;
		float swap;
		for (int i = 0; i < numChannels; i++) {
			swap = t1.data;
			t1.data = t2.data;
			t2.data = swap;
			if (t1.nextChannel != null && t2.nextChannel != null) {
				t1 = t1.nextChannel();
				t2 = t2.nextChannel();
			}
		}

	}

	/**
	 * Reverse the SoundList.
	 */
	public void reverse() {

		Link front = head;
		Link last = tail;
		Link reversed = head;
		int middle = (numSamples / numChannels) / 2;

		for (int i = 0; i < middle; i++) {
			swap(front, last);// call swap

			front = front.nextSample();

			reversed = front;
			while (reversed.nextSample() != last && reversed.nextSample() != tail) {
				reversed = reversed.nextSample();
			}
			last = reversed;
		}
	}

	/**
	 * Add a single sample to the end of the SoundList. Throws an exception if
	 * the soundlist has more than 1 channel
	 *
	 * @param sample
	 *            The sample to add
	 */
	@Override
	public void addSample(float sample) throws IllegalArgumentException {
		if (numChannels > 1 || numChannels != 1) {
			throw new IllegalArgumentException("this is multichannel");
		}
		if (tail == null && head == null) {

			tail = head = new Link();
			tail.setSample(sample);
		}

		else {
			tail.setNextSample(new Link(sample, null));// setting next
			tail = tail.nextSample();// moving to next
		}
	}

	/**
	 * Adds a single sample for each channel to the end of the SoundList. Throws
	 * an exception if the size of the sample array is not the same as the
	 * number of channels in the sound list
	 *
	 * @param sample
	 *            Array of samples (one for each channel) to add to the end of
	 *            the SoundList
	 */
	@Override
	public void addSample(float[] sample) {

		if (sample.length > numChannels) {
			throw new IllegalArgumentException("sample array size and number of channels vary");
		}

		if (tail == null && head == null) { // empty list
			head = tail = new Link();
			tail.setSample(sample[0]);
			Link tmp = tail;
			for (int i = 1; i < sample.length; i++) {
				tmp.setNextChannel(new Link(sample[i], null));
				if (tmp.nextChannel() != null) {
					tmp = tmp.nextChannel();
				}

			}

		}

		else {

			Link tmp = tail;
			tail.setNextSample(new Link(sample[0], null));
			tail = tail.nextSample();
			for (int i = 1; i < sample.length; i++) {

				tmp.nextSample().setNextChannel(new Link(sample[i], null));// add
																			// to
																			// end

				tmp.nextChannel().setNextSample(tmp.nextSample().nextChannel());
				if (tmp.nextChannel() != null) {
					tmp = tmp.nextChannel();
				}
			}
		}
	}

	// helper method

	/**
	 * Return an iterator that traverses a single channel of the list
	 *
	 * @param channel
	 *            The channel to traverse
	 * @return the iterator to traverse the list
	 */

	@Override
	public Iterator iterator(int channel) {
		SingleChannelIterator iterator = new SingleChannelIterator(channel);
		return iterator;
	}

	/**
	 * Return an iterator that traverses the entire sample, returning an array
	 * floats (one for each channel)
	 *
	 * @return iterator
	 */
	@Override
	public Iterator<float[]> iterator() { // single iterator
		IteratorAll iterator = new IteratorAll();
		return iterator;
	}

	/**
	 * Trim the SoundList, by removing all samples before the startTime, and all
	 * samples past the end time. Note that if a SoundList represents an 8
	 * second sound, and we call clip(4,7), the new SoundList will be a 3-second
	 * sound (from seconds 4-7 in the old SoundList)
	 *
	 * @param startTime
	 *            Time to start (in seconds)
	 * @param endTime
	 *            Time to end clip (as measured from the front of the original
	 *            clip, in seconds)
	 */
	@Override
	public void clip(float startTime, float duration) {

		boolean change = false;

		Iterator<float[]> thisIter = this.iterator();

		// move to start time, ignoring previous links
		for (int i = 0; i < startTime * sampleRate; i++) {
			if (thisIter.hasNext()) {
				float[] nextSamples = thisIter.next();
			}
		}

		for (int n = 0; n < duration * sampleRate; n++) {// add samples

			if (!change) {
				change(thisIter.next(), change, this.numChannels);
				change = true;
			} else {
				change(thisIter.next(), change, this.numChannels);
			}

		}

	}

	/**
	 * Splice a new SoundList into this soundList. Both SoundLists will be
	 * modified. If the sampleRate of the clipToSplice is not the same as this
	 * SoundList, an exception is thrown.
	 *
	 * @param startSpliceTime
	 *            Time to start the splice
	 * @param clipToSplice
	 *            The other SoundClip to splice in.
	 */
	@Override
	public void spliceIn(float startSpliceTime, SoundList clipToSplice) {

		Iterator<float[]> iter = this.iterator();
		Iterator<float[]> splice = clipToSplice.iterator();

		boolean changedHead = false;

		if (clipToSplice.getSampleRate() != this.sampleRate) {
			throw new IllegalArgumentException("the samples rates are not same");// throwing
																					// exception
		}

		change(iter.next(), changedHead, this.numChannels);
		changedHead = true;
		for (int i = 0; i < startSpliceTime * sampleRate; i++) {
			change(iter.next(), changedHead, this.numChannels);
		}
		// first splice is required, swapping causes 1,4,2,3
		while (splice.hasNext()) {
			change(splice.next(), changedHead, this.numChannels);// move
																	// iterator
		}

		while (iter.hasNext()) {
			change(iter.next(), changedHead, this.numChannels);
		}

		this.numSamples += clipToSplice.getNumSamples();

	}

	/**
	 * Combine all channels into a single channel, by adding together all
	 * channels into a single channel.
	 *
	 * @param allowClipping
	 *            If allowClipping is true, then values greater than 1.0 or less
	 *            than -1.0 after the addition are clipped to fit in the range.
	 *            If allowClipping is false, then if any values are greater than
	 *            1.0 or less than -1.0, the entire sample is rescaled to fit in
	 *            the range.
	 */
	@Override
	public void makeMono(boolean allowClipping) {
		boolean rwechanging = false;
		float largest = -1.0f;
		Iterator<float[]> iter1 = this.iterator();
		while (iter1.hasNext()) {
			float newSample = 0.0f;
			float[] oldVals = iter1.next();
			for (int i = 0; i < oldVals.length; i++) {
				newSample += oldVals[i];
				if (newSample > largest) {
					largest = newSample;
				}
			}

			if (allowClipping && Math.abs(newSample) > 1.0f) {
				if (newSample > 1.0) {
					newSample = 1.0f;
				} else {
					newSample = -1.0f;
				}
			}

			float[] sampleArray = { newSample };

			if (!rwechanging) {
				change(sampleArray, rwechanging, 1);
				rwechanging = true;
			} else {
				change(sampleArray, rwechanging, 1);
			}

		}

		this.numChannels = 1;
	}

	/**
	 * Combines this SoundList with a new SoundList, by adding the samples
	 * together. This SoundList is modified. If the sampleRate of the
	 * clipTocombine is not the same as this SoundList, an exception is thrown.
	 * If a SoundList of length 3 seconds and a SoundList of length 7 seconds
	 * are combined, the result will be a SoundList of 7 seconds.
	 *
	 * @param clipToCombine
	 *            The clip to combine with this clip
	 * @param allowClipping
	 *            If allowClipping is true, then values greater than 1.0 or less
	 *            than -1.0 after the addition are clipped to fit in the range.
	 *            If allowClipping is false, then the entire sample is rescaled
	 */

	@Override
	public void combine(SoundList clipToCombine, boolean allowClipping) {
		if (clipToCombine.getSampleRate() != this.sampleRate) {
			throw new IllegalArgumentException("sample rates are different");
		}

		boolean rescale = false;
		Iterator<float[]> iter1 = this.iterator();
		Iterator<float[]> iter2 = clipToCombine.iterator();

		while (iter1.hasNext() && iter2.hasNext()) {

			float[] s1 = iter1.next();
			float[] s2 = iter2.next();

			float[] combined = new float[numChannels];
			for (int i = 0; i < numChannels; i++) {

				float newSamp = s1[i] + s2[i];

				combined[i] = newSamp;
			}

		}
		change(iter2.next(), rescale, this.numChannels);
	}

	private void change(float[] samples, boolean change, int numChannels) {

		if (change == false) {
			numSamples = 0;
			head = tail = null;
		}
		this.addSample(samples);

	}

	/**
	 * Returns a clone of this SoundList
	 *
	 * @return The cloned SoundList
	 */
	@Override
	public SoundList clone() {
		Iterator<float[]> iter = this.iterator();
		SoundLinkedList list = new SoundLinkedList(this.sampleRate, this.numChannels);
		while (iter.hasNext()) {
			float[] samples = iter.next();
			list.addSample(samples);

		}
		return list;
	}

	private class Link {

		private float data;
		private Link nextChannel;
		private Link nextSample;

		Link(float sample, Link nextChannel, Link nextSample) {
			this.data = sample;
			this.nextChannel = nextChannel;
			this.nextSample = nextSample;
			numSamples++;
		}

		Link(float sample, Link nextSample) {
			this.data = sample;
			this.nextSample = nextSample;
			numSamples++;
		}

		Link(Link next) {
			nextSample = next;
			numSamples++;
		}

		Link() {
			numSamples++;
		}

		Link nextSample() {
			return nextSample;
		}

		Link nextChannel() {
			return nextChannel;
		}

		void setNextSample(Link next) {
			nextSample = next;
		}

		void setNextChannel(Link next) {
			nextChannel = next;
		}

		void setSample(float sample) {

			this.data = sample;
		}

	}

	public String toString() {

		String result = "[";
		Link current = head;
		while (current.nextChannel() != null) {
			current = current.nextChannel();
			System.out.println("current is" + current);
			result += current.nextSample.data;
			if (current.nextChannel() != null) {
				result += ",";
			}
		}
		result += "]";

		return result;

	}

	// multple channel iterator
	private class IteratorAll implements Iterator<float[]> {

		private Link curr;
		private Link prev;

		public IteratorAll() {

			curr = head;
			prev = null;

		}

		@Override
		public boolean hasNext() {
			return curr != null && curr.nextSample() != null;
		}

		public float[] next() {
			float[] samples = new float[numChannels];
			prev = curr;
			Link temp = curr;
			samples[0] = curr.data;
			for (int i = 1; i < numChannels; i++) {
				if (temp.nextChannel() != null) {
					temp = temp.nextChannel();
				}
				samples[i] = temp.data;
			}
			if (hasNext()) {
				curr = curr.nextSample();
			}
			return samples;

		}

	}
	// only one channle iterator

	private class SingleChannelIterator implements Iterator<Float> {

		private Link curr;
		private Link prev;

		public SingleChannelIterator(int index) {

			curr = head;
			prev = null;

			for (int i = 0; i < index; i++) {
				curr = curr.nextChannel();
			}

		}

		public Float next() {

			if (!hasNext()) {
				return curr.data;
			} else {
				float sample = curr.data;
				prev = curr;
				curr = curr.nextSample();

				return sample;
			}
		}

		public boolean hasNext() {
			return curr != null && curr.nextSample() != null;

		}

	}

	public static void main(String[] args) {

		int channels = 3;
		SoundLinkedList testList = new SoundLinkedList(0.5f, channels);

		testList.addSample(new float[] { 0.3f, 0.1f, 0.2f });
		testList.addSample(new float[] { 0.3f, 0.3f, 0.3f });
		testList.addSample(new float[] { 0.1f, 0.3f, 0.2f });

		testList.addSample(new float[] { 0.1f, 0.3f, 0.2f });
		testList.addSample(new float[] { 0.1f, 0.3f, 0.2f });

		System.out.println("resulting list: " + testList.toString());
	}

}
