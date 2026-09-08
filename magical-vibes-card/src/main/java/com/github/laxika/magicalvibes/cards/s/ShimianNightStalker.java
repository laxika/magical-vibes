package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageFromTargetAttackingCreatureToSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "36")
@CardRegistration(set = "LEG", collectorNumber = "116")
public class ShimianNightStalker extends Card {

    public ShimianNightStalker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new RedirectAllDamageFromTargetAttackingCreatureToSelfEffect()),
                "{B}, {T}: All damage that would be dealt to you this turn by target attacking creature is dealt to this creature instead.",
                TargetFilters.attackingCreature()
        ));
    }
}
