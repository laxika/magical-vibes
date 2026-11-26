package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourcePermanentHauntingTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHauntedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "94")
public class KayaGhostHaunter extends Card {

    private static final String DAMAGE_EMBLEM_TEXT =
            "At the beginning of your upkeep, this emblem deals 3 damage to the owner of target haunted creature.";
    private static final String CONTROL_EMBLEM_TEXT =
            "At the beginning of your upkeep, gain control of target haunted creature for as long as it remains haunted.";

    private static final TargetFilter HAUNTED_CREATURE = new PermanentPredicateTargetFilter(
            new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentIsHauntedPredicate())),
            "Target must be a haunted creature");

    public KayaGhostHaunter() {
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new ExileSourcePermanentHauntingTargetEffect()),
                "0: Exile Kaya, Ghost Haunter haunting target creature.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                DealDamageEffect.to(Subjects.ownerOf(Subjects.target()), 3))
                                .withTargetFilter(HAUNTED_CREATURE),
                        DAMAGE_EMBLEM_TEXT)),
                "-1: You get an emblem with \"" + DAMAGE_EMBLEM_TEXT + "\"."));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                GainControlOfTargetEffect.whileMatches(Subjects.target(),
                                        new PermanentIsHauntedPredicate()))
                                .withTargetFilter(HAUNTED_CREATURE),
                        CONTROL_EMBLEM_TEXT)),
                "-2: You get an emblem with \"" + CONTROL_EMBLEM_TEXT + "\"."));
    }
}
