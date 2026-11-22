package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C15", collectorNumber = "15")
@CardRegistration(set = "KHC", collectorNumber = "44")
public class SyntheticDestiny extends Card {

    public SyntheticDestiny() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addSpellEffect(new ExileTargetPermanentEffect(Subjects.creaturesYouControl()));
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                        new RevealCardsFromTopUntilEffect(Subjects.you(), creatureCard, new AmountThisWay(StepKind.EXILE)),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)),
                        new ShuffleLibraryEffect(false))
                .evenIfNothingHappened());
    }
}
