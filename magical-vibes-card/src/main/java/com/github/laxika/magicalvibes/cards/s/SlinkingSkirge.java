package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "71")
public class SlinkingSkirge extends Card {

    public SlinkingSkirge() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{2}, Sacrifice Slinking Skirge: Draw a card."
        ));
    }
}
