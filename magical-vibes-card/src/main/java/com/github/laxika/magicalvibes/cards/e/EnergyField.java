package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "USG", collectorNumber = "73")
public class EnergyField extends Card {

    public EnergyField() {
        addEffect(EffectSlot.STATIC, PreventDamageFromOpponentSourcesEffect.allDamage());
        addEffect(EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new SacrificeSelfEffect());
    }
}
