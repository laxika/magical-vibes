package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "104")
@CardRegistration(set = "MMA", collectorNumber = "125")
public class PyromancersSwath extends Card {

    public PyromancersSwath() {
        addStaticEffect(new AdditionalControllerDamageEffect(2,
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL))));
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new DiscardHandEffect());
    }
}
