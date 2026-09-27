package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "40K", collectorNumber = "82")
public class NoiseMarine extends Card {

    public NoiseMarine() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToAnyTargetEffect(new SpellsCastThisTurn(CountScope.CONTROLLER)));
    }
}
