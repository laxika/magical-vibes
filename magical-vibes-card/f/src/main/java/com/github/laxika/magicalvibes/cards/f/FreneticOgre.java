package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "195")
public class FreneticOgre extends Card {

    public FreneticOgre() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new DiscardRandomCardCost(), new BoostSelfEffect(3, 0)),
                "{R}, Discard a card at random: This creature gets +3/+0 until end of turn."
        ));
    }
}
