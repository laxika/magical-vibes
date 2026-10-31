package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        new ConditionalEffect(
                                new ControlsPermanent(new PermanentIsCommanderPredicate(true)),
                                new BecomeMonarchEffect())))
                .addSpellEffect(new FightTargetsEffect());
    }
}
