package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "200")
public class TurtleDuck extends Card {

    public TurtleDuck() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new SetBasePowerToughnessEffect(4, null, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)
                ),
                "{3}: This creature has base power 4 and gains trample until end of turn."
        ));
    }
}
