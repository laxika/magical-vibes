package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "139")
public class KorvoldAndTheNobleThief extends Card {

    public KorvoldAndTheNobleThief() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect(3));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")));
    }
}
