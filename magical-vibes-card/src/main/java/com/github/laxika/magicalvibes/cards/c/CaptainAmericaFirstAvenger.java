package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "88")
public class CaptainAmericaFirstAvenger extends Card {

    public CaptainAmericaFirstAvenger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new UnattachEquipmentFromSourceCost(),
                        new DealDividedDamageEffect(new XValue(), null, DivisionMode.CHOSEN,
                                null, 3, true, false, false)),
                "{3}, Unattach an Equipment from Captain America: He deals damage equal to that Equipment's mana value divided as you choose among one, two, or three targets."));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new AttachTargetEquipmentToSourceEffect());
    }
}
