package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AttachTriggeringEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "41")
@CardRegistration(set = "SLD", collectorNumber = "731")
@CardRegistration(set = "SLD", collectorNumber = "1727")
@CardRegistration(set = "SIR", collectorNumber = "43")
public class SigardasAid extends Card {

    public SigardasAid() {
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.AURA)));
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.enters(Triggers.EQUIPMENT,
                ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new AttachTriggeringEquipmentToTargetCreatureEffect(),
                        "Attach that Equipment to target creature you control?"));
    }
}
