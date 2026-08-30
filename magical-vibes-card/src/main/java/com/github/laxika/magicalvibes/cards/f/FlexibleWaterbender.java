package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "50")
public class FlexibleWaterbender extends Card {

    public FlexibleWaterbender() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new WaterbendCost(3),
                        new SetBasePowerToughnessEffect(5, 2, GrantScope.SELF)
                ),
                "Waterbend {3}: This creature has base power and toughness 5/2 until end of turn."
        ));
    }
}
