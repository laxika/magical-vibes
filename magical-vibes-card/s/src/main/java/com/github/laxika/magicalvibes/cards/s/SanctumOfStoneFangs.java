package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "120")
public class SanctumOfStoneFangs extends Card {

    public SanctumOfStoneFangs() {
        PermanentCount shrinesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER);

        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new LoseLifeEffect(shrinesYouControl, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new GainLifeEffect(shrinesYouControl));
    }
}
