package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "184")
@CardRegistration(set = "MKM", collectorNumber = "354")
@CardRegistration(set = "MKM", collectorNumber = "383")
public class AgrusKosSpiritOfJustice extends Card {

    public AgrusKosSpiritOfJustice() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, exileIfSuspected())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, suspectIfNotSuspected())
                .addEffect(EffectSlot.ON_ATTACK, exileIfSuspected())
                .addEffect(EffectSlot.ON_ATTACK, suspectIfNotSuspected());
    }

    private ConditionalEffect exileIfSuspected() {
        return new ConditionalEffect(
                new TargetPermanentMatches(new PermanentIsSuspectedPredicate()),
                new ExileTargetPermanentEffect());
    }

    private ConditionalEffect suspectIfNotSuspected() {
        return new ConditionalEffect(
                new NotCondition(new TargetPermanentMatches(new PermanentIsSuspectedPredicate())),
                new SuspectEffect(GrantScope.TARGET));
    }
}
