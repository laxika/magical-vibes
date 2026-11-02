package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "283")
@CardRegistration(set = "9ED", collectorNumber = "262")
@CardRegistration(set = "STH", collectorNumber = "111")
@CardRegistration(set = "40K", collectorNumber = "219")
@CardRegistration(set = "C18", collectorNumber = "157")
public class Overgrowth extends Card {

    public Overgrowth() {
        target(TargetFilters.land())
                .addTrigger(Triggers.playerTapsLandForMana(),
                        new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN, 2)));
    }
}
