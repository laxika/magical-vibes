package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;

@CardRegistration(set = "ECL", collectorNumber = "170")
@CardRegistration(set = "ECL", collectorNumber = "326")
public class CelestialReunion extends Card {

    public CelestialReunion() {
        addEffect(EffectSlot.SPELL, BeholdCost.optionalChosenCreatureType(2));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(1),
                new CardTypePredicate(CardType.CREATURE),
                LibrarySearchDestination.HAND,
                new ManaValueBound(false, 0),
                1, false, false, false, null, true));
    }
}
