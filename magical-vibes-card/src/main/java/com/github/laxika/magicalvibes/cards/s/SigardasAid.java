package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTriggeringEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "41")
public class SigardasAid extends Card {

    public SigardasAid() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.AURA)));
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)));

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
                new MayEffect(new AttachTriggeringEquipmentToTargetCreatureEffect(),
                        "Attach that Equipment to target creature you control?"));
    }
}
