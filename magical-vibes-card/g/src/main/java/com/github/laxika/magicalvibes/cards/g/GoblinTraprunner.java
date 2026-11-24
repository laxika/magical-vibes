package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "130")
public class GoblinTraprunner extends Card {

    public GoblinTraprunner() {
        CreateTokenEffect goblinToken = new CreateTokenEffect(
                1, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), true);
        addTrigger(Triggers.attacks(),
                FlipUntilLoseEffect.flip(3, null),
                goblinToken.withAmount(new AmountThisWay(StepKind.FLIP)));
    }
}
