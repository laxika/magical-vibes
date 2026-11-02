package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "93")
public class MarkOfFury extends Card {

    public MarkOfFury() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE));
        addTrigger(Triggers.endStep(ActivePlayer.ANY), ReturnToHandEffect.self());
    }
}
