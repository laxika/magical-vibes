package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "45")
@CardRegistration(set = "SLD", collectorNumber = "478")
@CardRegistration(set = "WOT", collectorNumber = "25")
@CardRegistration(set = "WOT", collectorNumber = "71")
@CardRegistration(set = "WOT", collectorNumber = "91")
@CardRegistration(set = "FCA", collectorNumber = "31")
public class RhysticStudy extends Card {

    public RhysticStudy() {
        // Whenever an opponent casts a spell, you may draw a card unless that player pays {1}.
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                UnlessPaysEffect.unlessPays(Subjects.triggeringPlayer(), "{1}",
                                new MayEffect(new DrawCardEffect(1), "Draw a card?"))
                        .withPrompt("Pay {1}? If you don't, Rhystic Study's controller may draw a card."));
    }
}
