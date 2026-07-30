package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "120")
public class ZombieInfestation extends Card {

    public ZombieInfestation() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        CreateTokenEffect.blackZombie(1)
                ),
                "Discard two cards: Create a 2/2 black Zombie creature token."
        ));
    }
}
