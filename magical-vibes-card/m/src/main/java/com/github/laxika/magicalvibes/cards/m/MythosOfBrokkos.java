package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "168")
public class MythosOfBrokkos extends Card {

    public MythosOfBrokkos() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AllConditions(List.of(
                        new ColorSpentToCast(ManaColor.BLUE),
                        new ColorSpentToCast(ManaColor.BLACK))),
                new SearchLibraryEffect(null, LibrarySearchDestination.GRAVEYARD)));
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardIsPermanentPredicate(), new Fixed(2)));
    }
}
