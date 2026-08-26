package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CloakTopCardAndAttachSourceEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "50")
public class CrypticCoat extends Card {

    public CrypticCoat() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CloakTopCardAndAttachSourceEquipmentEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(ReturnToHandEffect.self()),
                "{1}{U}: Return this Equipment to its owner's hand."));
    }
}
