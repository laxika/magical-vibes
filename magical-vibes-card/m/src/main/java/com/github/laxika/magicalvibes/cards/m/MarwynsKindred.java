package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "16")
public class MarwynsKindred extends Card {

    public MarwynsKindred() {
        addSpellEffect(ConjureCardsEffect.of(List.of(
                ConjureCardsEffect.EachOf.Printing.of("DOM", "172", new Fixed(1)),
                ConjureCardsEffect.EachOf.Printing.of("LEA", "210", new XValue())), ConjureDestination.ontoBattlefield()));
    }
}
