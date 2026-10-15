package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetBasePowerToughnessToAmountUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "55")
public class StorvaldFrostGiantJarl extends Card {

    public StorvaldFrostGiantJarl() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(3));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(3), GrantScope.OWN_CREATURES));

        setAllowSharedTargets(true);
        ChooseOneEffect modes = ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature has base power and toughness 7/7 until end of turn",
                        new SetTargetBasePowerToughnessToAmountUntilEndOfTurnEffect(
                                new Fixed(7), new Fixed(7)), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature has base power and toughness 1/1 until end of turn",
                        new SetTargetBasePowerToughnessToAmountUntilEndOfTurnEffect(
                                new Fixed(1), new Fixed(1)), TargetFilters.creature())
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(modes));
        addEffect(EffectSlot.ON_ATTACK, new ChooseOneAtTriggerTimeEffect(modes));
    }
}
