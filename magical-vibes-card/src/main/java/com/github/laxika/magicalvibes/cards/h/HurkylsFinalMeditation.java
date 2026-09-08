package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BRO", collectorNumber = "52")
public class HurkylsFinalMeditation extends Card {

    public HurkylsFinalMeditation() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotControllerTurn(), new IncreaseOwnCastCostEffect(3)));
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addEffect(EffectSlot.SPELL, new EndTurnEffect());
    }
}
