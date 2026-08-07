package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "64")
public class CirclingVultures extends Card {

    public CirclingVultures() {
        // "You may discard this card any time you could cast an instant." A free hand ability whose
        // only cost is the intrinsic discard the engine already pays — hence no mana cost and no
        // resolution effects.
        addHandActivatedAbility(new ActivatedAbility(false, null, List.of(),
                "You may discard this card any time you could cast an instant."));

        // "At the beginning of your upkeep, sacrifice this creature unless you exile the top
        // creature card of your graveyard." Optional cost: with no creature card in the graveyard
        // it can't be paid and the Vultures are sacrificed without a prompt.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new ExileTopCardOfGraveyardCost(CardType.CREATURE),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
