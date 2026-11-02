package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetedSpellPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "70")
@CardRegistration(set = "KHM", collectorNumber = "305")
@CardRegistration(set = "SLD", collectorNumber = "1683")
public class OrvarTheAllForm extends Card {

    public OrvarTheAllForm() {
        addTrigger(Triggers.youCastSpell(),
                new CreateTokenCopyOfTargetedSpellPermanentEffect());
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
