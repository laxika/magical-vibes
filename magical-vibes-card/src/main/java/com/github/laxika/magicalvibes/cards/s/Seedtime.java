package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "JUD", collectorNumber = "130")
public class Seedtime extends Card {

    public Seedtime() {
        setCastCondition(new ControllerTurn());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new OpponentCastSpellThisTurn(new CardColorPredicate(CardColor.BLUE)),
                new ControllerExtraTurnEffect(1)));
    }
}
