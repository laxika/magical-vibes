package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LivingBreakthrough;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "57")
public class InventiveIteration extends Card {

    public InventiveIteration() {
        setBackFaceCard(new LivingBreakthrough());

        var creatureOrPlaneswalker = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be a creature or planeswalker.");
        target(creatureOrPlaneswalker, 0, 1)
                .addEffect(EffectSlot.SAGA_CHAPTER_I, ReturnToHandEffect.target());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(creatureOrPlaneswalker));

        var artifactInGraveyard = new GraveyardCardThreshold(1, new CardTypePredicate(CardType.ARTIFACT));
        // "If you can't, draw a card" is decided before the return, so the draw comes first and the
        // two branches stay mutually exclusive.
        addEffect(EffectSlot.SAGA_CHAPTER_II, ConditionalEffect.unless(new NotCondition(artifactInGraveyard),
                new DrawCardEffect()));
        addEffect(EffectSlot.SAGA_CHAPTER_II, ConditionalEffect.unless(artifactInGraveyard,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.ARTIFACT))
                        .mandatory(true)
                        .build()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "LivingBreakthrough";
    }
}
