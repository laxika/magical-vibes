package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "213")
public class PardicSwordsmith extends Card {

    public PardicSwordsmith() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new DiscardRandomCardCost(), new BoostSelfEffect(2, 0)),
                "{R}, Discard a card at random: This creature gets +2/+0 until end of turn."
        ));
    }
}
