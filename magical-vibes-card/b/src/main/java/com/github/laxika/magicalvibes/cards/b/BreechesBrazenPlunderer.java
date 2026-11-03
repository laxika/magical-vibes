package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPG", collectorNumber = "6")
@CardRegistration(set = "LCC", collectorNumber = "217")
public class BreechesBrazenPlunderer extends Card {

    public BreechesBrazenPlunderer() {
        addTrigger(Triggers.creaturesYouControlDealDamageToOpponent()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.PIRATE)),
                new ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect());
    }
}
