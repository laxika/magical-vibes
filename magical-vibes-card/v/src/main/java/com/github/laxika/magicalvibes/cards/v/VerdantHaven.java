package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "199")
@CardRegistration(set = "M15", collectorNumber = "206")
@CardRegistration(set = "GTC", collectorNumber = "138")
public class VerdantHaven extends Card {

    public VerdantHaven() {
        // Enchant land
        // When this Aura enters, you gain 2 life.
        // Whenever enchanted land is tapped for mana, its controller adds an additional one mana of any color.
        target(TargetFilters.land())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2))
                .addTrigger(Triggers.enchantedLandTappedForMana(),
                        new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect()));
    }
}
