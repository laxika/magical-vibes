package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellTargetCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasAnyTargetPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "23")
@CardRegistration(set = "TDC", collectorNumber = "63")
public class VoraciousBibliophile extends Card {

    public VoraciousBibliophile() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryHasAnyTargetPredicate()),
                new DrawCardEffect(TriggeringSpellTargetCount.allTargets()));
    }
}
