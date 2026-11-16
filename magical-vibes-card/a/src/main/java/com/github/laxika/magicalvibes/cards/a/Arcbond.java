package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "91")
public class Arcbond extends Card {

    public Arcbond() {
        target(TargetFilters.creature())
                .addSpellEffect(new RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dealtDamage(), new MassDamageEffect(
                                new EventValue(), true, false,
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))
                                .dealtByTriggeringPermanent())));
    }
}
