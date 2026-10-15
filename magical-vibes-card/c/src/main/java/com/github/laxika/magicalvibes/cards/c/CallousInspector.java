package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TLA", collectorNumber = "89")
public class CallousInspector extends Card {

    public CallousInspector() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER),
                CreateTokenEffect.ofClueToken(1)));
    }
}
