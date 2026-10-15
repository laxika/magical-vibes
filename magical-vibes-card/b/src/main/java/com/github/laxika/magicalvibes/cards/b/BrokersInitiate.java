package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "5")
public class BrokersInitiate extends Card {

    public BrokersInitiate() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G/U}",
                List.of(new SetBasePowerToughnessEffect(5, 5, GrantScope.SELF)),
                "{4}{G/U}: This creature has base power and toughness 5/5 until end of turn."
        ));
    }
}
