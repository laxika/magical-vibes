package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardAndShuffleTopCardsEffect;

@CardRegistration(set = "40K", collectorNumber = "17")
public class TriumphOfSaintKatherine extends Card {

    public TriumphOfSaintKatherine() {
        addEffect(EffectSlot.ON_DEATH,
                new ExileSourceCardFromGraveyardAndShuffleTopCardsEffect(6));
        addCastingOption(new MiracleCast("{1}{W}"));
    }
}
