package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "76")
public class Necratog extends Card {

    public Necratog() {
        // The cost walks up from the top of the graveyard to the nearest creature card, skipping
        // noncreature cards; with no creature card at all the ability can't be activated.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileTopCardOfGraveyardCost(CardType.CREATURE),
                        new BoostSelfEffect(2, 2)
                ),
                "Exile the top creature card of your graveyard: This creature gets +2/+2 until end of turn."
        ));
    }
}
