package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "74")
public class MausoleumWanderer extends Card {

    public MausoleumWanderer() {
        // Whenever another Spirit you control enters, this creature gets +1/+1 until end of turn.
        // ON_ALLY_CREATURE_ENTERS_BATTLEFIELD already excludes the source ("another").
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.SPIRIT),
                new BoostSelfEffect(1, 1)));

        // Sacrifice this creature: Counter target instant or sorcery spell unless its controller
        // pays {X}, where X is this creature's power (snapshotted at payment).
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(true), new CounterUnlessPaysEffect(0, true, false)),
                "Sacrifice this creature: Counter target instant or sorcery spell unless its "
                        + "controller pays {X}, where X is this creature's power.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        "Target must be an instant or sorcery spell."
                )
        ));
    }
}
