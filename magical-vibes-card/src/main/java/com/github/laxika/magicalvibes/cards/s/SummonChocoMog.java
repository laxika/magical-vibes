package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "FIN", collectorNumber = "35")
public class SummonChocoMog extends Card {

    public SummonChocoMog() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        var stampede = new BoostAllOwnCreaturesEffect(1, 0, otherCreatures);
        addEffect(EffectSlot.SAGA_CHAPTER_I, stampede);
        addEffect(EffectSlot.SAGA_CHAPTER_II, stampede);
        addEffect(EffectSlot.SAGA_CHAPTER_III, stampede);
        addEffect(EffectSlot.SAGA_CHAPTER_IV, stampede);
    }
}
