package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "434")
@CardRegistration(set = "SLX", collectorNumber = "14")
public class ImmardTheStormcleaver extends Card {

    public ImmardTheStormcleaver() {
        var removalMode = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Immard deals 4 damage to any target",
                        new DealDamageToAnyTargetEffect(4)),
                new ChooseOneEffect.ChooseOneOption(
                        "Immard gains lifelink and indestructible until end of turn",
                        new GrantKeywordEffect(
                                Set.of(Keyword.LIFELINK, Keyword.INDESTRUCTIBLE), GrantScope.SELF))));

        var counterMode = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a charge counter on Immard",
                        new PutCountersOnSelfEffect(CounterType.CHARGE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove a charge counter from Immard",
                        new RemoveCounterFromSourceThenEffect(CounterType.CHARGE, removalMode))));

        var triggeredCounterMode = new ChooseOneAtTriggerTimeEffect(counterMode);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, triggeredCounterMode);
        addEffect(EffectSlot.ON_ATTACK, triggeredCounterMode);
    }
}
