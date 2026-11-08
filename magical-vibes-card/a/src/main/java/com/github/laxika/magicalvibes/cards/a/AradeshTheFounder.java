package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentEnlistedCreatureThisCombatPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "9")
@CardRegistration(set = "PIP", collectorNumber = "362")
@CardRegistration(set = "PIP", collectorNumber = "890")
@CardRegistration(set = "PIP", collectorNumber = "537")
public class AradeshTheFounder extends Card {

    public AradeshTheFounder() {
        // Whenever a creature you control attacks, if it enlisted a creature this combat, the creature that
        // attacked gains double strike until end of turn. If that creature's power is 4 or greater, draw a card.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringPermanentMatches(new PermanentEnlistedCreatureThisCombatPredicate())),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TRIGGERING_PERMANENT),
                new ConditionalEffect(new TriggeringPermanentPowerAtLeast(4), new DrawCardEffect(1)));
    }
}
