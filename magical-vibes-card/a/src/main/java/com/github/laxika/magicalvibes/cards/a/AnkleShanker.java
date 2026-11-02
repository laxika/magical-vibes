package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "164")
@CardRegistration(set = "C16", collectorNumber = "178")
public class AnkleShanker extends Card {

    public AnkleShanker() {
        addTrigger(Triggers.attacks(),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.DEATHTOUCH), GrantScope.ALL_OWN_CREATURES));
    }
}
