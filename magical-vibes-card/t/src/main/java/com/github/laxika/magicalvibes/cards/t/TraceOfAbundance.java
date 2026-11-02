package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "142")
@CardRegistration(set = "SLD", collectorNumber = "1849")
public class TraceOfAbundance extends Card {

    public TraceOfAbundance() {
        // Enchant land.
        target(TargetFilters.land())
                // Enchanted land has shroud.
                .addStaticEffect(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ENCHANTED_PERMANENT))
                // Whenever enchanted land is tapped for mana, its controller adds an additional one mana of any color.
                .addTrigger(Triggers.playerTapsLandForMana(),
                        new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect()));
    }
}
