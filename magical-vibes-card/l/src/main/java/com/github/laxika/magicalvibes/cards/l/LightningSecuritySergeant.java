package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "462")
@CardRegistration(set = "FIN", collectorNumber = "560")
public class LightningSecuritySergeant extends Card {

    public LightningSecuritySergeant() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                ExileTopCardOfOwnLibraryEffect.of(Subjects.you()),
                AllowPlayCardsFromExileEffect.whileYouControlSource(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
