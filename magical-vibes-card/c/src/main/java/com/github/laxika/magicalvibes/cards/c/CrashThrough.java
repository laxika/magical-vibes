package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "HOU", collectorNumber = "88")
@CardRegistration(set = "M19", collectorNumber = "133")
@CardRegistration(set = "AKR", collectorNumber = "149")
@CardRegistration(set = "M21", collectorNumber = "140")
@CardRegistration(set = "SLD", collectorNumber = "591")
public class CrashThrough extends Card {

    public CrashThrough() {
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addSpellEffect(new DrawCardEffect(1));
    }
}
