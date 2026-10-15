package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "61")
public class BarrowGhoul extends Card {

    public BarrowGhoul() {
        // "At the beginning of your upkeep, sacrifice this creature unless you exile the top
        // creature card of your graveyard." Optional cost: with no creature card in the graveyard
        // it can't be paid and the Ghoul is sacrificed without a prompt.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new ExileTopCardOfGraveyardCost(CardType.CREATURE),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
