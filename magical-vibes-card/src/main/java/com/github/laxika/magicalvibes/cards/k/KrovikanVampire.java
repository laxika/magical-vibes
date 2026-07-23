package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceDamagedCreatureDiedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamagedCreaturesThatDiedUnderControlEffect;

@CardRegistration(set = "ICE", collectorNumber = "141")
public class KrovikanVampire extends Card {

    public KrovikanVampire() {
        // At the beginning of each end step, if a creature dealt damage by this creature this turn
        // died, put that card onto the battlefield under your control. Sacrifice it when you lose
        // control of this creature.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new SourceDamagedCreatureDiedThisTurn(),
                new ReturnDamagedCreaturesThatDiedUnderControlEffect()));
    }
}
