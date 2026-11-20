package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;

@CardRegistration(set = "WOE", collectorNumber = "1")
@CardRegistration(set = "WOE", collectorNumber = "323")
public class ArchonOfTheWildRose extends Card {

    public ArchonOfTheWildRose() {
        var enchantedByAuraYouControl = new PermanentIsEnchantedBySourceControllerAuraPredicate();
        addStaticEffect(new SetBasePowerToughnessEffect(
                4, 4, Subjects.otherCreaturesYouControl(), enchantedByAuraYouControl));
        addStaticEffect(new GrantKeywordEffect(
                Keyword.FLYING, Subjects.otherCreaturesYouControl(), enchantedByAuraYouControl));
    }
}
