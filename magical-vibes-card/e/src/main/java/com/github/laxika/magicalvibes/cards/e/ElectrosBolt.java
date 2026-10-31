package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "77")
@CardRegistration(set = "OM1", collectorNumber = "78")
public class ElectrosBolt extends Card {

    public ElectrosBolt() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(4));
        addCastingOption(new GraveyardCast(null, "{1}{R}", List.of(), new CardDiscardedThisTurn()));
    }
}
