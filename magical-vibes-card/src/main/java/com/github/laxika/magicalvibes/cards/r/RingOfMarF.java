package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawFromOutsideGameReplacementEffect;

import java.util.List;

@CardRegistration(set = "ME1", collectorNumber = "163")
public class RingOfMarF extends Card {

    public RingOfMarF() {
        addActivatedAbility(new ActivatedAbility(true, "{5}",
                List.of(new ExileSelfCost(), new RegisterNextDrawFromOutsideGameReplacementEffect()),
                "{5}, {T}, Exile this artifact: The next time you would draw a card this turn, instead put a card you own from outside the game into your hand."));
    }
}
