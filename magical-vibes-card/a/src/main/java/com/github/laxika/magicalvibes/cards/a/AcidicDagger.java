package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeSourceWhenTargetLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;

@CardRegistration(set = "MIR", collectorNumber = "291")
public class AcidicDagger extends Card {

    public AcidicDagger() {
        // {4}, {T}: Whenever target creature deals combat damage to a non-Wall creature this turn,
        // destroy that non-Wall creature. When the targeted creature leaves the battlefield this
        // turn, sacrifice this artifact. Activate only before blockers are declared.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect(new TriggeredAbility(
                                Triggers.dealsDamageToCreature().withCombatDamageOnly().withDamagedFilter(
                                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL))),
                                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true))),
                        new RegisterDelayedSacrificeSourceWhenTargetLeavesEffect()
                ),
                "{4}, {T}: Whenever target creature deals combat damage to a non-Wall creature this "
                        + "turn, destroy that non-Wall creature. When the targeted creature leaves the "
                        + "battlefield this turn, sacrifice this artifact. Activate only before blockers "
                        + "are declared.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"),
                null,
                null,
                ActivationTimingRestriction.BEFORE_BLOCKERS_DECLARED
        ));
    }
}
