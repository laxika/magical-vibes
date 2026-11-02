package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "17")
public class FuelTankFeaster extends Card {

    public FuelTankFeaster() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect(1));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
