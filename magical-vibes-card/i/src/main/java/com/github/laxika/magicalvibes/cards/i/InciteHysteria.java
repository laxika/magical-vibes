package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "132")
public class InciteHysteria extends Card {

    public InciteHysteria() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        StaticAbility.of(new CantBlockEffect()), Subjects.withCreaturesSharingAColor(Subjects.target())));
    }
}
