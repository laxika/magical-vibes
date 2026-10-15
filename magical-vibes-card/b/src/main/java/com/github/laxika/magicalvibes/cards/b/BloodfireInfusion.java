package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "57")
public class BloodfireInfusion extends Card {

    public BloodfireInfusion() {
        target(TargetFilters.creatureYouControl());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsHostOfSourceAuraPredicate(),
                                "the enchanted creature",
                                true,
                                true),
                        new MassDamageEffect(new XValue(), false)),
                "{R}, Sacrifice enchanted creature: This Aura deals damage equal to the sacrificed "
                        + "creature's power to each creature."
        ));
    }
}
