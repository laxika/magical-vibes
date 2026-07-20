package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "149")
public class SwelteringSuns extends Card {

    public SwelteringSuns() {
        // Sweltering Suns deals 3 damage to each creature. (Non-targeting board sweep.)
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3));

        // Cycling {3} ({3}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new DrawCardEffect(1)),
                "Cycling {3} ({3}, Discard this card: Draw a card.)"));
    }
}
