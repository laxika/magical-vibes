package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "87")
public class ZombieScavengers extends Card {

    public ZombieScavengers() {
        // Exile the top creature card of your graveyard: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileTopCardOfGraveyardCost(CardType.CREATURE), new RegenerateEffect()),
                "Exile the top creature card of your graveyard: Regenerate this creature."
        ));
    }
}
