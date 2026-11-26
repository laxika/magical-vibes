package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "146")
public class ScourgeOfTheNobilis extends Card {

    public ScourgeOfTheNobilis() {
        target(TargetFilters.creature())
                // As long as enchanted creature is red, it gets +1/+1...
                .addStaticEffect(new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)),
                        new StaticBoostEffect(1, 1, Set.of(), Subjects.attachedCreature()),
                        null
                ))
                // ...and has "{R/W}: This creature gets +1/+0 until end of turn."
                .addStaticEffect(new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        false,
                                        "{R/W}",
                                        List.of(BoostEffect.self(1, 0)),
                                        "{R/W}: This creature gets +1/+0 until end of turn."
                                ),
                                Subjects.attachedCreature()
                        ),
                        null
                ))
                // As long as enchanted creature is white, it gets +1/+1 and has lifelink.
                .addStaticEffect(new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new StaticBoostEffect(1, 1, Set.of(Keyword.LIFELINK), Subjects.attachedCreature()),
                        null
                ));
    }
}
