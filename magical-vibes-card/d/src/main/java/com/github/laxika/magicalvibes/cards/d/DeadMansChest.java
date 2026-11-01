package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "66")
@CardRegistration(set = "AFC", collectorNumber = "97")
public class DeadMansChest extends Card {

    public DeadMansChest() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect());
    }
}
