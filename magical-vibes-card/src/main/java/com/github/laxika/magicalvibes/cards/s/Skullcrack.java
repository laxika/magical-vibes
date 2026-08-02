package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;

@CardRegistration(set = "GTC", collectorNumber = "106")
public class Skullcrack extends Card {

    public Skullcrack() {
        // Players can't gain life this turn.
        addEffect(EffectSlot.SPELL, new PlayersCantGainLifeThisTurnEffect());

        // Damage can't be prevented this turn.
        addEffect(EffectSlot.SPELL, new DamageCantBePreventedThisTurnEffect());

        // Skullcrack deals 3 damage to target player or planeswalker.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(3));
    }
}
