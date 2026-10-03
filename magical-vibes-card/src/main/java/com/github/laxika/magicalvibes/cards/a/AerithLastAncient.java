package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect;

@CardRegistration(set = "FIC", collectorNumber = "76")
@CardRegistration(set = "FIC", collectorNumber = "163")
@CardRegistration(set = "FIC", collectorNumber = "471")
public class AerithLastAncient extends Card {

    public AerithLastAncient() {
        // Raise — At the beginning of your end step, if you gained life this turn, return target
        // creature card from your graveyard to your hand. If you gained 7 or more life, return it
        // to the battlefield instead. The existing battlefield-or-hand effect uses a dynamic
        // mana-value bound; the two bounds make the threshold branch independent of the target's
        // mana value while preserving one graveyard target.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect(
                        new FixedIfCondition(new GainedLifeThisTurn(7), Integer.MAX_VALUE, -1))));
    }
}
