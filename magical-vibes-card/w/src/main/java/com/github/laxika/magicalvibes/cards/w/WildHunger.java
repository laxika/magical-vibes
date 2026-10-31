package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "132")
@CardRegistration(set = "INR", collectorNumber = "225")
@CardRegistration(set = "INR", collectorNumber = "424")
@CardRegistration(set = "UMA", collectorNumber = "193")
public class WildHunger extends Card {

    public WildHunger() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
