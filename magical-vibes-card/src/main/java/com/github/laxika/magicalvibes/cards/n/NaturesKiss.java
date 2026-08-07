package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "136")
public class NaturesKiss extends Card {

    public NaturesKiss() {
        // Enchant creature
        target(TargetFilters.creature());

        // {1}, Exile the top card of your graveyard: Enchanted creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ExileTopCardOfGraveyardCost(),
                        new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(1), new Fixed(1))),
                "{1}, Exile the top card of your graveyard: Enchanted creature gets +1/+1 until end of turn."
        ));
    }
}
