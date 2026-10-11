package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "BRO", collectorNumber = "155")
public class UnleashShell extends Card {

    public UnleashShell() {
        addSimultaneousEffects(EffectSlot.SPELL,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(5),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
