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
public class BeaconOfUnrest extends Card {

    public BeaconOfUnrest() {
        addEffect(EffectSlot.SPELL, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.BATTLEFIELD,
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)
                )),
                GraveyardSearchScope.ALL_GRAVEYARDS
        ));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
