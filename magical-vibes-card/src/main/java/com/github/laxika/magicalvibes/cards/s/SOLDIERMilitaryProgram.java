package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "25")
@CardRegistration(set = "FIC", collectorNumber = "108")
public class SOLDIERMilitaryProgram extends Card {

    public SOLDIERMilitaryProgram() {
        var commander = new ControllerControlsCommander();
        var options = List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 white Soldier creature token",
                        CreateTokenEffect.whiteSoldier(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on each of up to two Soldiers you control",
                        new PutCounterOnChosenPermanentsEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 2,
                                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER))));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                ConditionalEffect.unless(commander, ChooseOneEffect.oneOrMore(options)));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                ConditionalEffect.unless(new NotCondition(commander), new ChooseOneEffect(options)));
    }
}
