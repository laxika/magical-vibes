package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellAndPlotEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "4")
public class AvenInterrupter extends Card {

    public AvenInterrupter() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTruePredicate(), "Target must be a spell."))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetSpellAndPlotEffect());
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTruePredicate(), 2, CostModificationScope.OPPONENT,
                Set.of(Zone.GRAVEYARD, Zone.EXILE)));
    }
}
