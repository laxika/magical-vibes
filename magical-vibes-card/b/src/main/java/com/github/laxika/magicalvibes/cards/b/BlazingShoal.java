package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "96")
public class BlazingShoal extends Card {

    public BlazingShoal() {
        // Target creature gets +X/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new XValue(), new Fixed(0)));
        // You may exile a red card with mana value X from your hand rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                ExileCardsFromHandCastingCost.withManaValueX(new CardColorPredicate(CardColor.RED), "red"))));
    }
}
