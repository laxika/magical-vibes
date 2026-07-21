package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EndStepPlayerDidntCastCreatureSpell;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "58")
public class PredatoryAdvantage extends Card {

    public PredatoryAdvantage() {
        // At the beginning of each opponent's end step, if that player didn't cast a creature spell
        // this turn, create a 2/2 green Lizard creature token.
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new ConditionalEffect(
                new EndStepPlayerDidntCastCreatureSpell(),
                new CreateTokenEffect("Lizard", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.LIZARD), Set.of(), Set.of())));
    }
}
