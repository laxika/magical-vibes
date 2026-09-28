package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanentsAndSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MSC", collectorNumber = "83")
@CardRegistration(set = "MSC", collectorNumber = "401")
public class FirstFamily extends Card {

    public FirstFamily() {
        // You draw X cards and gain X life, where X is the number of colors among permanents you
        // control and spells you've cast this turn.
        var colors = new ColorsAmongControlledPermanentsAndSpellsCastThisTurn();
        addEffect(EffectSlot.SPELL, new DrawCardEffect(colors));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(colors));
    }
}
