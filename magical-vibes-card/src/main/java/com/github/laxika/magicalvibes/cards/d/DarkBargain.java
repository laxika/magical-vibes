package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;

@CardRegistration(set = "DOM", collectorNumber = "83")
public class DarkBargain extends Card {

    public DarkBargain() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsChooseNToHandRestToGraveyardEffect(3, 2));
        addEffect(EffectSlot.SPELL, new DealDamageToControllerEffect(2));
    }
}
