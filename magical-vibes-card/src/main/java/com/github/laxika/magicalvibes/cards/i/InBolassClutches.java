package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;

@CardRegistration(set = "DOM", collectorNumber = "54")
public class InBolassClutches extends Card {

    public InBolassClutches() {
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
        addEffect(EffectSlot.STATIC, new GrantSupertypeToEnchantedPermanentEffect(CardSupertype.LEGENDARY));
    }
}
