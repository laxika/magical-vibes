package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "65")
@CardRegistration(set = "MSC", collectorNumber = "379")
public class FightForTheThrone extends Card {

    public FightForTheThrone() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new ResolveEffectOnTargetDeathThisTurnEffect(
                        new ConditionalEffect(
                                new ControlsPermanent(new PermanentIsCommanderPredicate()),
                                new BecomeMonarchEffect())))
                .addEffect(EffectSlot.SPELL, new FightTargetsEffect());
    }
}
