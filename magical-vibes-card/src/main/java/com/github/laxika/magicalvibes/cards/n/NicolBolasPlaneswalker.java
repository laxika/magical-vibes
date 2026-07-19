package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "120")
public class NicolBolasPlaneswalker extends Card {

    public NicolBolasPlaneswalker() {
        // +3: Destroy target noncreature permanent.
        addActivatedAbility(new ActivatedAbility(
                +3,
                List.of(new DestroyTargetPermanentEffect()),
                "+3: Destroy target noncreature permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                        "Target must be a noncreature permanent"
                )
        ));

        // −2: Gain control of target creature.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                "−2: Gain control of target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // −9: Nicol Bolas deals 7 damage to target player or planeswalker. That player or that
        // planeswalker's controller discards seven cards, then sacrifices seven permanents of their
        // choice. Discard and sacrifice piggyback on the damage effect's player-or-planeswalker target.
        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(7),
                        new DiscardEffect(7, DiscardRecipient.TARGET_PLAYER_OR_PERMANENT_CONTROLLER),
                        new SacrificePermanentsEffect(
                                7, new PermanentTruePredicate(),
                                SacrificeRecipient.TARGET_PLAYER_OR_PERMANENT_CONTROLLER)
                ),
                "−9: Nicol Bolas, Planeswalker deals 7 damage to target player or planeswalker. "
                        + "That player or that planeswalker's controller discards seven cards, then "
                        + "sacrifices seven permanents of their choice."
        ));
    }
}
