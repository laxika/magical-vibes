package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "79")
@CardRegistration(set = "FIC", collectorNumber = "166")
@CardRegistration(set = "FIC", collectorNumber = "472")
public class BarretAvalancheLeader extends Card {

    public BarretAvalancheLeader() {
        addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Rebel", 2, 2, CardColor.RED,
                        List.of(CardSubtype.REBEL), Set.of(), Set.of()));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"), 0, 1);
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.REBEL),
                "Target must be a Rebel you control"));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new AttachTargetEquipmentToTargetCreatureEffect());
    }
}
