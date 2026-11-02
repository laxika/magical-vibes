package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "22")
public class SageOfTheSkies extends Card {

    public SageOfTheSkies() {
        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfConditionEffect(
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), true, false, true));
    }
}
