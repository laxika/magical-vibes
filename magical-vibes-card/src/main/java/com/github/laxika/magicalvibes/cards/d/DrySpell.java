package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "POR", collectorNumber = "90")
public class DrySpell extends Card {

    public DrySpell() {
        // Dry Spell deals 1 damage to each creature and each player.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(1, true));
    }
}
