package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "115")
@CardRegistration(set = "INR", collectorNumber = "381")
@CardRegistration(set = "AVR", collectorNumber = "106")
@CardRegistration(set = "MM3", collectorNumber = "72")
@CardRegistration(set = "SLD", collectorNumber = "160")
@CardRegistration(set = "SLD", collectorNumber = "974")
@CardRegistration(set = "SLD", collectorNumber = "975")
@CardRegistration(set = "SLD", collectorNumber = "976")
@CardRegistration(set = "SLD", collectorNumber = "1620")
public class Griselbrand extends Card {

    public Griselbrand() {
        // Pay 7 life: Draw seven cards.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(7), new DrawCardEffect(7)),
                "Pay 7 life: Draw seven cards."));
    }
}
