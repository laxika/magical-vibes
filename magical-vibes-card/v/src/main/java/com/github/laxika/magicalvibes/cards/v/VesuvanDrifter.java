package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "MAT", collectorNumber = "10")
@CardRegistration(set = "MAT", collectorNumber = "60")
@CardRegistration(set = "MAT", collectorNumber = "110")
@CardRegistration(set = "MAT", collectorNumber = "156")
@CardRegistration(set = "MAT", collectorNumber = "194")
public class VesuvanDrifter extends Card {

    public VesuvanDrifter() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY), new MayEffect(
                SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(Subjects.you()),
                        BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.REVEAL,
                                        new CardTypePredicate(CardType.CREATURE)))
                                .untilEndOfTurn().except(CopyExceptions.withKeywords(Keyword.FLYING))),
                "Reveal the top card of your library?")
        );
    }
}
