package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOC", collectorNumber = "143")
public class Drumbellower extends Card {

    public Drumbellower() {
        addEffect(EffectSlot.STATIC, new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                TurnStep.UNTAP, new PermanentIsCreaturePredicate()));
    }
}
