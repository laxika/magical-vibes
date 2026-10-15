package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "GPT", collectorNumber = "71")
public class ParallectricFeedback extends Card {

    public ParallectricFeedback() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new TargetSpellManaValue(), DamageRecipient.TARGET_SPELL_CONTROLLER));
    }
}
