package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LukkaWaywardBonder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksPlaneswalker;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "153")
public class MilaCraftyCompanion extends Card {

    public MilaCraftyCompanion() {
        LukkaWaywardBonder backFace = new LukkaWaywardBonder();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new OpponentAttacksPlaneswalker(),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.LOYALTY, 1, new PermanentIsPlaneswalkerPredicate())));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Mila, Crafty Companion", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Lukka, Wayward Bonder", backFace.getEffects(EffectSlot.SPELL)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "LukkaWaywardBonder";
    }
}
