package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "82")
public class SickeningShoal extends Card {

    public SickeningShoal() {
        // Target creature gets -X/-X until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
        addCastingOption(new AlternateHandCast(List.of(
                ExileCardsFromHandCastingCost.withManaValueX(new CardColorPredicate(CardColor.BLACK), "black"))));
    }
}
