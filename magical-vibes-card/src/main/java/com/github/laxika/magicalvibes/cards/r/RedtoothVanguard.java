package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "WOE", collectorNumber = "180")
public class RedtoothVanguard extends Card {

    public RedtoothVanguard() {
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new MayPayManaEffect("{2}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {2} to return Redtooth Vanguard from your graveyard to your hand?"));
    }
}
