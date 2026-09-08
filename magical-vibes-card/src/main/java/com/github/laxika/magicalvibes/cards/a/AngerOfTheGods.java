package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "THS", collectorNumber = "112")
@CardRegistration(set = "AKR", collectorNumber = "138")
public class AngerOfTheGods extends Card {

    public AngerOfTheGods() {
        // Anger of the Gods deals 3 damage to each creature. If a creature dealt damage this way
        // would die this turn, exile it instead.
        addEffect(EffectSlot.SPELL, MassDamageEffect.exilingDamageToEachCreature(3));
    }
}
