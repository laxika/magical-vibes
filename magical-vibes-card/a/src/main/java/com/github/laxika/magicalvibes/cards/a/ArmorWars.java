package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "203")
public class ArmorWars extends Card {

    public ArmorWars() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(new PermanentCount(
                                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)),
                        new EachOpponentDrawsCardEffect(1)),
                "Draw cards for each artifact you control?"));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(
                        new CardTypePredicate(CardType.ARTIFACT), 1));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new DealDamageToPlayersEffect(
                        new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()),
                        DamageRecipient.TARGET_PLAYER));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Must target an opponent")));
    }
}
