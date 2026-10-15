package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "73")
@CardRegistration(set = "PIP", collectorNumber = "601")
public class BighornerRancher extends Card {

    public BighornerRancher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, new GreatestPowerAmongControlled())),
                "{T}: Add an amount of {G} equal to the greatest power among creatures you control."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GainLifeEffect(new GreatestToughnessAmongControlled(
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))),
                "Sacrifice this creature: You gain life equal to the greatest toughness among other creatures you control."
        ));
    }
}
