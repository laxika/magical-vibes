package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "22")
public class SageOfTheSkies extends Card {

    public SageOfTheSkies() {
        // "When you cast this spell, if you've cast another spell this turn": the intervening "if".
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerCastAnotherSpellThisTurn(new CardTruePredicate())),
                new CopyTriggeringSpellEffect(true));
    }
}
