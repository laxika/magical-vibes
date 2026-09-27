package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivationCostEffect;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "56")
public class LoreseekersStone extends Card {

    public LoreseekersStone() {
        addActivatedAbility(new ActivatedAbility(true, "{3}", List.of(
                new IncreaseActivationCostEffect(new CardsInHand(CountScope.CONTROLLER)),
                new DrawCardEffect(3)),
                "{3}, {T}: Draw three cards. This ability costs {1} more to activate for each card in your hand."));
    }
}
