package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "MB1", collectorNumber = "8")
public class Metagamer extends Card {

    public Metagamer() {
        // Taxes spells whose name matches a card in the latest Mythic Championship winning deck. The
        // engine models no tournament results, so the winning-deck pool is empty and no spell matches.
        addStaticEffect(new IncreaseSpellCostEffect(
                new CardNotPredicate(new CardTruePredicate()), 1, CostModificationScope.ALL));
    }
}
