package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;

@CardRegistration(set = "DOM", collectorNumber = "213")
@CardRegistration(set = "DMR", collectorNumber = "219")
public class DampingSphere extends Card {

    public DampingSphere() {
        addStaticEffect(new ReplaceLandExcessManaWithColorlessEffect());
        addStaticEffect(new IncreaseSpellCostEffect(
                new CardTruePredicate(), new SpellsCastThisTurn(CountScope.CONTROLLER), CostModificationScope.ALL));
    }
}
