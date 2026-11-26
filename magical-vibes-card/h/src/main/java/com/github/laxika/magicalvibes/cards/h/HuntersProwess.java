package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "124")
@CardRegistration(set = "E02", collectorNumber = "33")
@CardRegistration(set = "E01", collectorNumber = "66")
@CardRegistration(set = "PIO", collectorNumber = "381")
@CardRegistration(set = "C17", collectorNumber = "152")
@CardRegistration(set = "SCD", collectorNumber = "192")
public class HuntersProwess extends Card {

    public HuntersProwess() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                new DrawCardEffect(new EventValue()))));
    }
}
