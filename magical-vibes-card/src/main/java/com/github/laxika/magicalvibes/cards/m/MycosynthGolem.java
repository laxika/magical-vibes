package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "137")
public class MycosynthGolem extends Card {

    public MycosynthGolem() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(artifactsYouControl));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)
                )), artifactsYouControl, CostModificationScope.SELF));
    }
}
