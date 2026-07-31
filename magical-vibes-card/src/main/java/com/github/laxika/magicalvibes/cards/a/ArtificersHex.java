package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureAttachedToEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M14", collectorNumber = "85")
public class ArtificersHex extends Card {

    public ArtificersHex() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment"
        ));
        // At the beginning of your upkeep, if enchanted Equipment is attached to a creature, destroy that creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DestroyCreatureAttachedToEnchantedEquipmentEffect());
    }
}
