package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.UntapAllCreaturesOfChosenTypeEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "107")
public class RiptideChronologist extends Card {

    public RiptideChronologist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new UntapAllCreaturesOfChosenTypeEffect()),
                "{U}, Sacrifice this creature: Untap all creatures of the creature type of your choice."
        ));
    }
}
