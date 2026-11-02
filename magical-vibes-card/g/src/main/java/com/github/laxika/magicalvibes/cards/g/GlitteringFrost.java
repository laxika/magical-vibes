package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "171")
public class GlitteringFrost extends Card {

    public GlitteringFrost() {
        target(TargetFilters.land())
                .addStaticEffect(new GrantSupertypeToEnchantedPermanentEffect(CardSupertype.SNOW))
                .addTrigger(Triggers.playerTapsLandForMana(),
                        new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect()));
    }
}
