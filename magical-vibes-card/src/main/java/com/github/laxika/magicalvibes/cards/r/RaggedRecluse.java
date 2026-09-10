package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OdiousWitch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "VOW", collectorNumber = "127")
public class RaggedRecluse extends Card {

    public RaggedRecluse() {
        setBackFaceCard(new OdiousWitch());

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new ControllerDiscardedCardThisTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "OdiousWitch";
    }
}
