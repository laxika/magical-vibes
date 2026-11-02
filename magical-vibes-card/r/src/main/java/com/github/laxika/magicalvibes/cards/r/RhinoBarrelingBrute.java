package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "140")
@CardRegistration(set = "OM1", collectorNumber = "160")
public class RhinoBarrelingBrute extends Card {

    public RhinoBarrelingBrute() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardMinManaValuePredicate(4, true)),
                new DrawCardEffect()));
    }
}
