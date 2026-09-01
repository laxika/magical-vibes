package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FireLordSozin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "117")
public class TheRiseOfSozin extends Card {

    public TheRiseOfSozin() {
        setBackFaceCard(new FireLordSozin());

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        )).addEffect(EffectSlot.SAGA_CHAPTER_II,
                new ChooseCardNameAndExileFromZonesEffect(List.of(), 4));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "FireLordSozin";
    }
}
