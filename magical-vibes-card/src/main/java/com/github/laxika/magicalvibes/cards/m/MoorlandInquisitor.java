package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "30")
public class MoorlandInquisitor extends Card {

    public MoorlandInquisitor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{2}{W}: Moorland Inquisitor gains first strike until end of turn."
        ));
    }
}
