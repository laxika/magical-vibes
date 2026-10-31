package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SeekThreeLandsThenChooseEffect;

@CardRegistration(set = "YTDM", collectorNumber = "25")
public class SongOfSeasons extends Card {

    public SongOfSeasons() {
        addSpellEffect(new SeekThreeLandsThenChooseEffect());
    }
}
