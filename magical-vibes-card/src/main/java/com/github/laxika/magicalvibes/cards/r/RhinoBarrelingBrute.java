package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

@CardRegistration(set = "SPM", collectorNumber = "140")
public class RhinoBarrelingBrute extends Card {

    public RhinoBarrelingBrute() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardMinManaValuePredicate(4)),
                new DrawCardEffect()));
    }
}
