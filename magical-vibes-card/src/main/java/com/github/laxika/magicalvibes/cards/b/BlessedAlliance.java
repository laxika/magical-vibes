package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "13")
public class BlessedAlliance extends Card {

    public BlessedAlliance() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}"));
        setAllowSharedTargets(true);

        var anyPlayerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");
        var attackingCreatureFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate()));

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains 4 life",
                        new TargetPlayerGainsLifeEffect(4),
                        anyPlayerFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap up to two target creatures",
                        List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                        creatureFilter, null, 0, 2, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent sacrifices an attacking creature of their choice",
                        new SacrificePermanentsEffect(
                                1, attackingCreatureFilter, SacrificeRecipient.TARGET_PLAYER),
                        opponentFilter)
        )));
    }
}
