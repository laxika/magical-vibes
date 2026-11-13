package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceExertedThisTurn;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "125")
@CardRegistration(set = "AKR", collectorNumber = "148")
@CardRegistration(set = "LTC", collectorNumber = "212")
@CardRegistration(set = "BLC", collectorNumber = "194")
public class CombatCelebrant extends Card {

    public CombatCelebrant() {
        addStaticEffect(new ExertAsItAttacksEffect(new NotCondition(new SourceExertedThisTurn())));
        addTrigger(Triggers.exertedAsItAttacks(), SequenceEffect.of(
                new UntapPermanentsEffect(TapUntapScope.OTHER_CONTROLLED_CREATURES),
                new AdditionalCombatPhaseEffect(1)));
    }
}
