package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "63")
@CardRegistration(set = "LEG", collectorNumber = "184")
public class EmeraldDragonfly extends Card {

    public EmeraldDragonfly() {
        addActivatedAbility(new ActivatedAbility(false, "{G}{G}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)), "{G}{G}: This creature gains first strike until end of turn."));
    }
}
