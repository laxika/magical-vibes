package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetPermanentUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CyclingTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "31")
public class CrystallineResonance extends Card {

    public CrystallineResonance() {
        BecomeCopyOfTargetPermanentUntilYourNextTurnEffect copyEffect =
                new BecomeCopyOfTargetPermanentUntilYourNextTurnEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        Triggers.youDiscardCard());
        addTrigger(Triggers.youDiscardCard(),
                new CyclingTriggerEffect(new MayEffect(
                        copyEffect,
                        "Have Crystalline Resonance become a copy of that permanent until your next turn?")));
    }
}
