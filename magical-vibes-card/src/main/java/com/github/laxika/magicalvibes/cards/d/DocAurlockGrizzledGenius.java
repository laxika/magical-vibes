package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "201")
public class DocAurlockGrizzledGenius extends Card {

    public DocAurlockGrizzledGenius() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 2, CostModificationScope.SELF,
                Set.of(Zone.GRAVEYARD, Zone.EXILE)));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 2, CostModificationScope.SELF,
                Set.of(Zone.HAND), true));
    }
}
