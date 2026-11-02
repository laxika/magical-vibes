package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "211")
@CardRegistration(set = "8ED", collectorNumber = "248")
@CardRegistration(set = "INV", collectorNumber = "188")
@CardRegistration(set = "USG", collectorNumber = "252")
@CardRegistration(set = "BRB", collectorNumber = "26")
@CardRegistration(set = "HOP", collectorNumber = "71")
@CardRegistration(set = "DDE", collectorNumber = "52")
@CardRegistration(set = "SLD", collectorNumber = "1846")
@CardRegistration(set = "ECC", collectorNumber = "106")
@CardRegistration(set = "AFC", collectorNumber = "158")
@CardRegistration(set = "PIP", collectorNumber = "198")
@CardRegistration(set = "PIP", collectorNumber = "726")
@CardRegistration(set = "WOC", collectorNumber = "126")
@CardRegistration(set = "C18", collectorNumber = "147")
public class FertileGround extends Card {

    public FertileGround() {
        target(TargetFilters.land())
                .addTrigger(Triggers.playerTapsLandForMana(),
                        new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect()));
    }
}
