package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ICE", collectorNumber = "42")
@CardRegistration(set = "ME3", collectorNumber = "18")
public class LightningBlow extends Card {

    public LightningBlow() {
        // "Target creature gains first strike until end of turn."
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
        // "Draw a card at the beginning of the next turn's upkeep."
        addSpellEffect(new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
