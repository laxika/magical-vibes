package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OdiousWitch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "127")
@CardRegistration(set = "DBL", collectorNumber = "394")
public class RaggedRecluse extends Card {

    public RaggedRecluse() {
        setBackFaceCard(new OdiousWitch());

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new ControllerDiscardedCardThisTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "OdiousWitch";
    }
}
