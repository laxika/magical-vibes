package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "108")
@CardRegistration(set = "PIP", collectorNumber = "636")
@CardRegistration(set = "PIP", collectorNumber = "417")
@CardRegistration(set = "PIP", collectorNumber = "945")
public class MacCreadyLamplightMayor extends Card {

    public MacCreadyLamplightMayor() {
        // Whenever a creature you control with power 2 or less attacks, it gains skulk until end
        // of turn.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentPowerAtMostPredicate(2),
                        new GrantKeywordEffect(Keyword.SKULK, GrantScope.TARGET)));

        // Whenever a creature with power 4 or greater attacks you, its controller loses 2 life and
        // you gain 2 life.
        addTrigger(Triggers.creatureAttacks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingSourceControllerPredicate(),
                                new PermanentPowerAtLeastPredicate(4))),
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER),
                                new GainLifeEffect(2))));
    }
}
