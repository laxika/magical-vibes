package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "118")
public class HeatedArgument extends Card {

    public HeatedArgument() {
        // Heated Argument deals 6 damage to target creature. You may exile a card from your
        // graveyard. If you do, Heated Argument also deals 2 damage to that creature's controller.
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(6))
                .addSpellEffect(new MayEffect(
                        new ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect(2),
                        "Exile a card from your graveyard?"
                ));
    }
}
