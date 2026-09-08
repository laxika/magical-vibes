package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DistinctColorPairsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "220")
@CardRegistration(set = "MKM", collectorNumber = "319")
@CardRegistration(set = "MKM", collectorNumber = "368")
public class NivMizzetGuildpact extends Card {

    public NivMizzetGuildpact() {
        DistinctColorPairsAmongControlledPermanents colorPairs = new DistinctColorPairsAmongControlledPermanents();
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"
        );
        TargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
        setAllowSharedTargets(true);

        target(anyTarget).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DealDamageToAnyTargetEffect(colorPairs));
        target(anyPlayer).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DrawCardForTargetPlayerEffect(colorPairs, false, true));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new GainLifeEffect(colorPairs));
    }
}
