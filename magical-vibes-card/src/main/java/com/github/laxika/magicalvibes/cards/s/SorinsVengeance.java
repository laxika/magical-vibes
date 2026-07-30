package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M12", collectorNumber = "111")
public class SorinsVengeance extends Card {

    public SorinsVengeance() {
        // Deals 10 damage to target player or planeswalker and you gain 10 life. The life gain
        // declares no target; it piggybacks on the damage effect's target and fizzles with it.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(10));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(10));
    }
}
