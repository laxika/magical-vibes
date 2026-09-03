package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SNC", collectorNumber = "29")
public class RumorGatherer extends Card {

    public RumorGatherer() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                SequenceEffect.of(
                        ConditionalEffect.unless(
                                new NotCondition(new NthAbilityResolutionThisTurn(2)), new ScryEffect(1)),
                        ConditionalEffect.unless(
                                new NthAbilityResolutionThisTurn(2), new DrawCardEffect(1))));
    }
}
