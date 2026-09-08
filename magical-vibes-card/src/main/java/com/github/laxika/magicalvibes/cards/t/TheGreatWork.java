package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromAnyGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnFrontEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

public class TheGreatWork extends Card {

    public TheGreatWork() {
        var opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        target(opponent)
                .addEffect(EffectSlot.SAGA_CHAPTER_I,
                        new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToPermanentsTargetControlsEffect(3));

        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofTreasureToken(3));

        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.SAGA_CHAPTER_III, SequenceEffect.of(
                new AllowCastMatchingCardsFromAnyGraveyardThisTurnEffect(instantOrSorcery),
                new ExileSelfAndReturnFrontEffect()));
    }
}
