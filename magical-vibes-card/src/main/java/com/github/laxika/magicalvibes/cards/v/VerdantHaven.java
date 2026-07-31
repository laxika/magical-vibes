package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M14", collectorNumber = "199")
public class VerdantHaven extends Card {

    public VerdantHaven() {
        // Enchant land
        // When this Aura enters, you gain 2 life.
        // Whenever enchanted land is tapped for mana, its controller adds an additional one mana of any color.
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2))
                .addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect()));
    }
}
