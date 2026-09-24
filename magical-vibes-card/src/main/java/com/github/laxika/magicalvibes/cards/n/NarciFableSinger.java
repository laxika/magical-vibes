package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "CMM", collectorNumber = "710")
@CardRegistration(set = "CMM", collectorNumber = "775")
public class NarciFableSinger extends Card {

    public NarciFableSinger() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsEnchantmentPredicate(),
                        new DrawCardEffect(1)));
        addEffect(EffectSlot.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES,
                SequenceEffect.of(
                        new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(new EventValue())));
    }
}
