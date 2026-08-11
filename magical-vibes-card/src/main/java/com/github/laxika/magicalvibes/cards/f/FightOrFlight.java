package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeparateCreaturesIntoPilesAndChooseAttackersEffect;

@CardRegistration(set = "INV", collectorNumber = "16")
public class FightOrFlight extends Card {

    public FightOrFlight() {
        addEffect(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED,
                new SeparateCreaturesIntoPilesAndChooseAttackersEffect());
    }
}
