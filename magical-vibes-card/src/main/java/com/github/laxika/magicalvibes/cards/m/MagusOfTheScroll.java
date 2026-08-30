package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealRandomCardFromHandDealDamageEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "169")
public class MagusOfTheScroll extends Card {

    public MagusOfTheScroll() {
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new ChooseNameRevealRandomCardFromHandDealDamageEffect(2)),
                "{3}, {T}: Choose a card name, then reveal a card at random from your hand. "
                        + "If that card has the chosen name, Magus of the Scroll deals 2 damage to any target."));
    }
}
