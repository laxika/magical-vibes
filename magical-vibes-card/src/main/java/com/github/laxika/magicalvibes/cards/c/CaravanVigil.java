package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MorbidReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "173")
public class CaravanVigil extends Card {

    public CaravanVigil() {
        // Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        // Morbid — If a creature died this turn, you may put that card onto the battlefield instead.
        addEffect(EffectSlot.SPELL, new MorbidReplacementEffect(
                new SearchLibraryForBasicLandToHandEffect(),
                new SearchLibraryForCardTypesToBattlefieldEffect(Set.of(CardType.LAND), true, false)
        ));
    }
}
