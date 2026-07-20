package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FreeCyclingWhileHandSizeEffect;

@CardRegistration(set = "AKH", collectorNumber = "63")
public class NewPerspectives extends Card {

    public NewPerspectives() {
        // When this enchantment enters, draw three cards.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(3));

        // As long as you have seven or more cards in hand, you may pay {0} rather than pay cycling
        // costs. Static alternative cost read at cycling activation (AbilityActivationService); the
        // card being cycled still counts toward the seven.
        addEffect(EffectSlot.STATIC, new FreeCyclingWhileHandSizeEffect(7));
    }
}
