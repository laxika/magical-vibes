package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "35")
public class CloudheathDrake extends Card {

    public CloudheathDrake() {
        // {1}{W}: This creature gains vigilance until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                "{1}{W}: Cloudheath Drake gains vigilance until end of turn."));
    }
}
