package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "265")
public class Leashling extends Card {

    public Leashling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PutCardFromHandOnTopOfLibraryCost(), ReturnToHandEffect.self()),
                "Put a card from your hand on top of your library: Return this creature to its owner's hand."
        ));
    }
}
