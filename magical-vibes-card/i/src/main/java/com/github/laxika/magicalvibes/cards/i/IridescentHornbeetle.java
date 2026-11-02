package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.PlusOnePlusOneCountersPutOnControlledCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "187")
public class IridescentHornbeetle extends Card {

    public IridescentHornbeetle() {
        // At the beginning of your end step, create a 1/1 green Insect creature token for each
        // +1/+1 counter you've put on creatures under your control this turn.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new CreateTokenEffect(
                new PlusOnePlusOneCountersPutOnControlledCreaturesThisTurn(),
                "Insect",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.INSECT),
                Set.of(),
                Set.of()
        ));
    }
}
