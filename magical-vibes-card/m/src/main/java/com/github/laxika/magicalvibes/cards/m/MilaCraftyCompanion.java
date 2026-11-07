package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LukkaWaywardBonder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksPlaneswalker;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "153")
public class MilaCraftyCompanion extends Card {

    public MilaCraftyCompanion() {
        LukkaWaywardBonder backFace = new LukkaWaywardBonder();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new OpponentAttacksPlaneswalker()),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.LOYALTY, 1, new PermanentIsPlaneswalkerPredicate()));
        addTrigger(Triggers.permanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Mila, Crafty Companion", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Lukka, Wayward Bonder", backFace.getSpellEffects()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "LukkaWaywardBonder";
    }
}
