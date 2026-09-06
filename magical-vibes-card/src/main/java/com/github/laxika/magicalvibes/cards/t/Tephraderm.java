package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceCreatureOrSpellControllerEffect;

@CardRegistration(set = "ONS", collectorNumber = "239")
public class Tephraderm extends Card {

    public Tephraderm() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DealDamageToDamageSourceCreatureOrSpellControllerEffect());
    }
}
