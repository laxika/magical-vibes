package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;

@CardRegistration(set = "YDMU", collectorNumber = "20")
public class ArvadWeatherlightSmuggler extends Card {

    public ArvadWeatherlightSmuggler() {
        DynamicAmount creaturesDiedThisTurn = new CreatureDeathsThisTurn(CountScope.ANY_PLAYER);
        CardEffect boost = new ConditionalEffect(new Morbid(),
                new PerpetuallyBoostCardEffect(this, creaturesDiedThisTurn, creaturesDiedThisTurn));

        // At the beginning of your end step, if a creature died this turn, Arvad perpetually gets
        // +X/+X, where X is the number of creatures that died this turn.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, boost);
        // This ability also triggers if Arvad is in your graveyard.
        addEffect(EffectSlot.GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED, boost);
    }
}
