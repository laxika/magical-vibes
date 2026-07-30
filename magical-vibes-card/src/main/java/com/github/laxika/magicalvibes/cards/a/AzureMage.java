package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "45")
public class AzureMage extends Card {

    public AzureMage() {
        // {3}{U}: Draw a card. No tap in the cost, so it is repeatable with enough mana.
        addActivatedAbility(new ActivatedAbility(false, "{3}{U}",
                List.of(new DrawCardEffect(1)),
                "{3}{U}: Draw a card."));
    }
}
