package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "TDM", collectorNumber = "22")
public class SageOfTheSkies extends Card {

    public SageOfTheSkies() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfConditionEffect(
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), true));
    }
}
