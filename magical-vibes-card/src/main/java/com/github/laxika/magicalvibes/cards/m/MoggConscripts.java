package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TMP", collectorNumber = "189")
public class MoggConscripts extends Card {

    public MoggConscripts() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControllerCastSpellThisTurn(new CardTypePredicate(CardType.CREATURE)),
                "you've cast a creature spell this turn"
        ));
    }
}
