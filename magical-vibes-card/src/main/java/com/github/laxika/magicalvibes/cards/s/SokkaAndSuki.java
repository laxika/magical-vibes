package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "71")
public class SokkaAndSuki extends Card {

    public SokkaAndSuki() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"), 0, 1)
                .addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.ALLY),
                                new AttachTargetEquipmentToTriggeringPermanentEffect()));

        addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Ally", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.ALLY), Set.of(), Set.of()));
    }
}
