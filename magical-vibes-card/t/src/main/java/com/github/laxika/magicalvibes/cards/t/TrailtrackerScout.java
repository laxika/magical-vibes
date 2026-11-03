package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "35")
@CardRegistration(set = "BLC", collectorNumber = "68")
public class TrailtrackerScout extends Card {

    public TrailtrackerScout() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addTrigger(Triggers.youCastSpell().expend(8),
                ReturnTargetCardsFromGraveyardToHandEffect.forTriggeredAbility(new CardIsPermanentPredicate(), 1));
    }
}
