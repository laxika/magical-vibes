package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTriggeringEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "2XM", collectorNumber = "260")
public class HammerOfNazahn extends Card {

    public HammerOfNazahn() {
        // Whenever Hammer of Nazahn or another Equipment you control enters, you may attach that
        // Equipment to target creature you control.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new AttachSourceEquipmentToTargetCreatureEffect(),
                                "Attach Hammer of Nazahn to target creature you control?"))
                .addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
                        new MayEffect(new AttachTriggeringEquipmentToTargetCreatureEffect(),
                                "Attach that Equipment to target creature you control?"));

        // Equipped creature gets +2/+0 and has indestructible.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.EQUIPPED_CREATURE));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
