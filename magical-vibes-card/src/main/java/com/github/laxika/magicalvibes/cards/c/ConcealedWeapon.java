package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "117")
public class ConcealedWeapon extends Card {

    public ConcealedWeapon() {
        addMorph("{2}{R}");
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, new AttachSourceEquipmentToTargetCreatureEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{1}{R}"));
    }
}
