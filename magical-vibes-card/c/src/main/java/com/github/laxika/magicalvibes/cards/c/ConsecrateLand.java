package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachedPermanentRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSB", collectorNumber = "4")
@CardRegistration(set = "2ED", collectorNumber = "15")
@CardRegistration(set = "LEA", collectorNumber = "14")
@CardRegistration(set = "LEB", collectorNumber = "15")
public class ConsecrateLand extends Card {

    public ConsecrateLand() {
        target(TargetFilters.land())
                .addStaticEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_PERMANENT))
                .addStaticEffect(new AttachedPermanentRestrictionEffect(new CantBeEnchantedByOtherAurasEffect()));
    }
}
