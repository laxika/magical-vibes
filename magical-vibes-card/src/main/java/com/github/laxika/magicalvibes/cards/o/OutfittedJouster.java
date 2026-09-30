package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndSacrificeAttachedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YWOE", collectorNumber = "22")
public class OutfittedJouster extends Card {

    public OutfittedJouster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConjureCardToBattlefieldEffect("Steelclaw Lance"),
                new ConjureCardToBattlefieldEffect("Brawler's Plate"),
                new AttachCreatedEquipmentToSourceEffect()));

        addEffect(EffectSlot.STATIC, new PreventDamageAndSacrificeAttachedEquipmentEffect());
    }
}
