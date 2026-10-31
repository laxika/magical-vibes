package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "201")
public class DocAurlockGrizzledGenius extends Card {

    public DocAurlockGrizzledGenius() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 2, CostModificationScope.SELF,
                Set.of(Zone.GRAVEYARD, Zone.EXILE)));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 2, CostModificationScope.SELF,
                Set.of(Zone.HAND), true));
    }
}
