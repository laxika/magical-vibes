package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "778")
@CardRegistration(set = "SLD", collectorNumber = "781")
@CardRegistration(set = "SLD", collectorNumber = "784")
@CardRegistration(set = "SLD", collectorNumber = "918")
@CardRegistration(set = "SLD", collectorNumber = "1938")
@CardRegistration(set = "SLD", collectorNumber = "2010")
@CardRegistration(set = "SLD", collectorNumber = "2011")
@CardRegistration(set = "SLD", collectorNumber = "2012")
@CardRegistration(set = "SLD", collectorNumber = "2013")
@CardRegistration(set = "SLD", collectorNumber = "2064")
public class Food extends Card {

    public Food() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{2}, {T}, Sacrifice this token: You gain 3 life."
        ));
    }
}
