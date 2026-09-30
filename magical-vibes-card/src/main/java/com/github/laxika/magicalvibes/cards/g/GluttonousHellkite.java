package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M3C", collectorNumber = "73")
@CardRegistration(set = "M3C", collectorNumber = "125")
public class GluttonousHellkite extends Card {

    public GluttonousHellkite() {
        addEffect(EffectSlot.ON_SELF_CAST, new SacrificePermanentsEffect(
                new XValue(), new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_PLAYER,
                false, true, false, true));
        addEffect(EffectSlot.ON_SELF_CAST,
                new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(
                        new Scaled(new EventValue(), 2)));
    }
}
