package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOC", collectorNumber = "139")
public class ChainsOfCustody extends Card {

    public ChainsOfCustody() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                        new CounterUnlessPaysEffect(2), GrantScope.ENCHANTED_CREATURE));

        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
