package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "122")
public class SkitteringHeartstopper extends Card {

    public SkitteringHeartstopper() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)), "{B}: Skittering Heartstopper gains deathtouch until end of turn."));
    }
}
