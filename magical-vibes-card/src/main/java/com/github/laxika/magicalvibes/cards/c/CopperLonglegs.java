package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "165")
public class CopperLonglegs extends Card {

    public CopperLonglegs() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new SacrificeSelfCost(), new ProliferateEffect()),
                "{1}{G}, Sacrifice Copper Longlegs: Proliferate."
        ));
    }
}
