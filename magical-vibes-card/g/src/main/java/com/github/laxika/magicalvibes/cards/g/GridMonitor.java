package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;

import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "183")
public class GridMonitor extends Card {

    public GridMonitor() {
        addStaticEffect(new CantCastSpellTypeEffect(Set.of(CardType.CREATURE)));
    }
}
