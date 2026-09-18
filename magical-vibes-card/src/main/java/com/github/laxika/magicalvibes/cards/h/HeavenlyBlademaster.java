package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachAnyNumberOfControlledAurasAndEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "GN3", collectorNumber = "11")
public class HeavenlyBlademaster extends Card {

    public HeavenlyBlademaster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new AttachAnyNumberOfControlledAurasAndEquipmentToSourceEffect());
        AttachmentsOnSource attachments = new AttachmentsOnSource(true, true);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(attachments, attachments,
                GrantScope.OWN_CREATURES));
    }
}
