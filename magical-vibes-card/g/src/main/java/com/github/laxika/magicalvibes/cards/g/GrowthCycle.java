package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M20", collectorNumber = "175")
public class GrowthCycle extends Card {

    public GrowthCycle() {
        // Target creature gets +3/+3 until end of turn. It gets an additional +2/+2 until end
        // of turn for each card named Growth Cycle in your graveyard.
        var namedCardsInGraveyard = new CardsInGraveyard(
                new CardNamedPredicate("Growth Cycle"), CountScope.CONTROLLER);
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Sum(new Fixed(3), new Scaled(namedCardsInGraveyard, 2)),
                new Sum(new Fixed(3), new Scaled(namedCardsInGraveyard, 2))));
    }
}
