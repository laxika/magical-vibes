package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "168")
public class OnceAndFuture extends Card {

    public OnceAndFuture() {
        List<CardPredicate> filters = List.of(new CardTruePredicate(), new CardTruePredicate());
        List<Integer> minimumTargetCounts = List.of(1, 0);
        List<String> targetDescriptions = List.of("card", "other card");

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ColorSpentToCast(ManaColor.GREEN, 3),
                new ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
                        filters,
                        List.of(GraveyardChoiceDestination.HAND,
                                GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY),
                        targetDescriptions,
                        minimumTargetCounts,
                        true),
                new ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
                        filters,
                        List.of(GraveyardChoiceDestination.HAND, GraveyardChoiceDestination.HAND),
                        targetDescriptions,
                        minimumTargetCounts,
                        true)));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
