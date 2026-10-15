package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CreatureAttackingController;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "OANA", collectorNumber = "3")
@CardRegistration(set = "ANB", collectorNumber = "6")
public class ConfrontTheAssault extends Card {

    public ConfrontTheAssault() {
        // Cast this spell only if a creature is attacking you.
        setCastCondition(new CreatureAttackingController());

        // Create three 1/1 white Spirit creature tokens with flying.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(3));
    }
}
