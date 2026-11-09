package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M3C", collectorNumber = "73")
@CardRegistration(set = "M3C", collectorNumber = "125")
public class GluttonousHellkite extends Card {

    public GluttonousHellkite() {
        addTrigger(Triggers.youCastThisSpell(),
                new SacrificePermanentsEffect(
                new XValue(), new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_PLAYER,
                false, true, false, true),
                new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(
                        new Scaled(new EventValue(), 2)));
    }
}
