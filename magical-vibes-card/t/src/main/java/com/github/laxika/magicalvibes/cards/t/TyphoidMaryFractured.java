package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RandomChoiceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "MSC", collectorNumber = "99")
@CardRegistration(set = "MSC", collectorNumber = "421")
public class TyphoidMaryFractured extends Card {

    private static final String MARY_MODE = "Mary — Create a Treasure token";
    private static final String TYPHOID_MARY_MODE = "Typhoid Mary — Draw a card";
    private static final String BLOODY_MARY_MODE =
            "Bloody Mary — Each opponent loses 2 life and you gain 2 life";

    public TyphoidMaryFractured() {
        ChooseOneEffect controllerChoice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(MARY_MODE, CreateTokenEffect.ofTreasureToken(1)),
                new ChooseOneEffect.ChooseOneOption(TYPHOID_MARY_MODE, new DrawCardEffect()),
                new ChooseOneEffect.ChooseOneOption(BLOODY_MARY_MODE, List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2)))
        ));

        CardEffect randomChoice = new RandomChoiceEffect(List.of(
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                List.of(new DrawCardEffect()),
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2))
        ));

        // "Choose one at random. If you discarded a card this turn, you choose one instead": the mode is chosen as the
        // ability is put on the stack (CR 700.2b), so which choice is made is part of the trigger event, an ability of
        // its own for each.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerDiscardedCardThisTurn()),
                controllerChoice);
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new ControllerDiscardedCardThisTurn())),
                randomChoice);
    }
}
