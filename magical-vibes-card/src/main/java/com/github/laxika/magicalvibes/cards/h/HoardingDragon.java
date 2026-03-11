package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCardIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;

import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "144")
public class HoardingDragon extends Card {

    public HoardingDragon() {
        // When Hoarding Dragon enters the battlefield, you may search your library
        // for an artifact card, exile it, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryForCardTypeToExileAndImprintEffect(Set.of(CardType.ARTIFACT)),
                        "Search your library for an artifact card to exile?"));

        // When Hoarding Dragon dies, you may put the exiled card into its owner's hand.
        addEffect(EffectSlot.ON_DEATH,
                new MayEffect(new PutImprintedCardIntoOwnersHandEffect(),
                        "Put the exiled card into its owner's hand?"));
    }
}
