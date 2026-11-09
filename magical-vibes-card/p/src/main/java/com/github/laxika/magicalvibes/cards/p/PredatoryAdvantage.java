package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.EndStepPlayerDidntCastSpell;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "58")
public class PredatoryAdvantage extends Card {

    public PredatoryAdvantage() {
        // At the beginning of each opponent's end step, if that player didn't cast a creature spell
        // this turn, create a 2/2 green Lizard creature token.
        addTrigger(Triggers.endStep(ActivePlayer.OPPONENT),
                TriggerOptions.DEFAULT.withInterveningIf(new EndStepPlayerDidntCastSpell(CardType.CREATURE)),
                new CreateTokenEffect("Lizard", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.LIZARD), Set.of(), Set.of()));
    }
}
