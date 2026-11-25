package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRK", collectorNumber = "88")
@CardRegistration(set = "TSB", collectorNumber = "85")
public class SpittingSlug extends Card {

    public SpittingSlug() {
        GrantKeywordEffect firstStrikeToSlug = new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.source());
        GrantKeywordEffect firstStrikeToCombatOpponents = new GrantKeywordEffect(
                Keyword.FIRST_STRIKE,
                Subjects.allCreatures(),
                new PermanentInCombatWithSourcePredicate());
        UnlessPaysEffect mayPay = UnlessPaysEffect.youMayPay("{1}{G}", firstStrikeToSlug)
                .ifNotPaid(firstStrikeToCombatOpponents)
                .withPrompt("Pay {1}{G} to give Spitting Slug first strike?");

        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, mayPay);
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.ONE_OR_MORE, mayPay);
    }
}
