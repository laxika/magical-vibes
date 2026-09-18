package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "85")
public class BraidssFrightfulReturn extends Card {

    public BraidssFrightfulReturn() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                        "a creature", false, false),
                "Sacrifice a creature?"));

        addEffect(EffectSlot.SAGA_CHAPTER_II, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());

        var nonlandNontoken = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new MayEffect(
                new SacrificePermanentsEffect(1, nonlandNontoken, SacrificeRecipient.TARGET_PLAYER),
                "Have the targeted opponent sacrifice a nonland, nontoken permanent?",
                SequenceEffect.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                        new DrawCardEffect(1)),
                MayChoicePlayer.TARGET_PLAYER));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
