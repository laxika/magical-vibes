package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "152")
public class PackMastiff extends Card {

    public PackMastiff() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0, new PermanentNamedPredicate("Pack Mastiff"))),
                "{1}{R}: Each creature you control named Pack Mastiff gets +1/+0 until end of turn."
        ));
    }
}
