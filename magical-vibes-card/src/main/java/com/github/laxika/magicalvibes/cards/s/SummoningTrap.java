package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCreatureSpellCounteredByOpponentThisTurn;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "184")
public class SummoningTrap extends Card {

    public SummoningTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(), new ControllerCreatureSpellCounteredByOpponentThisTurn(), false));
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayPutMatchingOntoBattlefield(
                7, new CardTypePredicate(CardType.CREATURE)));
    }
}
