package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "247")
public class DackFayden extends Card {

    public DackFayden() {
        // +1: Target player draws two cards, then discards two cards.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new DrawCardForTargetPlayerEffect(2),
                        new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)
                ),
                "+1: Target player draws two cards, then discards two cards.",
                anyPlayer()
        ));

        // −2: Gain control of target artifact.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                "−2: Gain control of target artifact.",
                TargetFilters.artifact()
        ));

        // −6: You get an emblem with "Whenever you cast a spell that targets one or more
        // permanents, gain control of those permanents."
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(
                                null,
                                List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                                new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate())
                        )),
                        "Whenever you cast a spell that targets one or more permanents, gain control of those permanents.")),
                "−6: You get an emblem with \"Whenever you cast a spell that targets one or more permanents, "
                        + "gain control of those permanents.\""
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }
}
