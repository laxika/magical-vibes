package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MBS", collectorNumber = "57")
public class VirulentWound extends Card {

    public VirulentWound() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(CounterType.MINUS_ONE_MINUS_ONE))
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        new GivePoisonCountersEffect(1, Subjects.controllerOf(Subjects.triggering()))));
    }
}
