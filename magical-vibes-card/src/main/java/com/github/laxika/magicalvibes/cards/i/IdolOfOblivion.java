package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControllerCreatedTokenThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "392")
@CardRegistration(set = "CMM", collectorNumber = "607")
public class IdolOfOblivion extends Card {

    public IdolOfOblivion() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect()),
                "{T}: Draw a card. Activate only if you created a token this turn."
        ).withActivationCondition(
                new ControllerCreatedTokenThisTurn(),
                "Activate only if you created a token this turn."));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                "Eldrazi", 10, 10, null,
                                List.of(CardSubtype.ELDRAZI), Set.of(), Set.of())),
                "{8}, {T}, Sacrifice this artifact: Create a 10/10 colorless Eldrazi creature token."));
    }
}
