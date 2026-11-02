package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControllerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "630")
public class MysteriosMirage extends Card {

    public MysteriosMirage() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new ControllerDiscardedCardThisTurn(),
                new CreateTokenEffect("Illusion Villain", 3, 3, CardColor.BLUE,
                        List.of(CardSubtype.ILLUSION, CardSubtype.VILLAIN), Set.of(), Set.of())));
    }
}
