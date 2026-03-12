package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;

import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "192")
public class PrimevalTitan extends Card {

    public PrimevalTitan() {
        // Whenever Primeval Titan enters the battlefield or attacks,
        // you may search your library for up to two land cards,
        // put them onto the battlefield tapped, then shuffle.
        MayEffect searchEffect = new MayEffect(
                new SearchLibraryForCardTypesToBattlefieldEffect(Set.of(CardType.LAND), false, true, 2),
                "You may search your library for up to two land cards, put them onto the battlefield tapped, then shuffle."
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, searchEffect);
        addEffect(EffectSlot.ON_ATTACK, searchEffect);
    }
}
