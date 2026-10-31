package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "222")
public class VigorousCharge extends Card {

    public VigorousCharge() {
        addStaticEffect(new KickerEffect("{W}"));
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                                new GainLifeEffect(new EventValue()), true)));
    }
}
