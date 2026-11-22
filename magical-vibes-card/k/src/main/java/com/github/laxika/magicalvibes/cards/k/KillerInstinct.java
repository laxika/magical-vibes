package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "120")
public class KillerInstinct extends Card {

    public KillerInstinct() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(new CardTypePredicate(CardType.CREATURE)))
                        .underControlOf(Subjects.you()),
                new GrantKeywordEffect(Keyword.HASTE, Subjects.returnedThisResolution()),
                RegisterDelayedStepTriggerEffect.aboutPutOntoBattlefield(
                        Triggers.endStep(ActivePlayer.ANY), SacrificeSelfEffect.ofTriggeringPermanent()));
    }
}
