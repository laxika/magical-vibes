package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "128")
public class GoblinBarrage extends Card {

    public GoblinBarrage() {
        // Kicker—Sacrifice an artifact or Goblin.
        addStaticEffect(new KickerEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                )),
                "an artifact or Goblin"
        ));

        // Goblin Barrage deals 4 damage to target creature.
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(4));

        // If this spell was kicked, it also deals 4 damage to target player or planeswalker.
        targetWhenKicked(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"), 0, 0, 1, 1)
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        DealDamageToAnyTargetEffect.forTargetGroup(4, 1)));
    }
}
