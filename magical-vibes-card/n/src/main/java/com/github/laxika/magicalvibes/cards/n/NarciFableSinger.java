package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "710")
@CardRegistration(set = "CMM", collectorNumber = "775")
public class NarciFableSinger extends Card {

    public NarciFableSinger() {
        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsEnchantmentPredicate(),
                        new DrawCardEffect(1)));
        addTrigger(Triggers.finalChapterAbilityResolves(),
                SequenceEffect.of(
                        new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(new EventValue())));
    }
}
