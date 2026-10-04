package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "129")
@CardRegistration(set = "5DN", collectorNumber = "41")
@CardRegistration(set = "HOP", collectorNumber = "18")
@CardRegistration(set = "2XM", collectorNumber = "77")
@CardRegistration(set = "40K", collectorNumber = "194")
@CardRegistration(set = "C19", collectorNumber = "105")
@CardRegistration(set = "C16", collectorNumber = "107")
@CardRegistration(set = "ARC", collectorNumber = "10")
public class BeaconOfUnrest extends Card {

    public BeaconOfUnrest() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)
                )))
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .targetGraveyard(true)
                .build());
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
