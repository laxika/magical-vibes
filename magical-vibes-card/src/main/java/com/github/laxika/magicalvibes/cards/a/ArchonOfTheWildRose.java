package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;

@CardRegistration(set = "WOE", collectorNumber = "1")
public class ArchonOfTheWildRose extends Card {

    public ArchonOfTheWildRose() {
        var enchantedByAuraYouControl = new PermanentIsEnchantedBySourceControllerAuraPredicate();
        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(
                4, 4, GrantScope.OWN_CREATURES, enchantedByAuraYouControl));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.OWN_CREATURES, enchantedByAuraYouControl));
    }
}
