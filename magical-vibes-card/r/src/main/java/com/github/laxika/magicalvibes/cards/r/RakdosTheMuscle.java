package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "226")
public class RakdosTheMuscle extends Card {

    public RakdosTheMuscle() {
        addTrigger(Triggers.sacrificed(new PermanentIsCreaturePredicate(), ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        ExileTopCardOfOwnLibraryEffect.of(Subjects.targetPlayer(), new TriggeringPermanentManaValue()),
                        new AllowPlayCardsFromExileEffect(Subjects.cardsThisWay(StepKind.EXILE),
                                Duration.UNTIL_YOUR_NEXT_END_STEP, false, true, false)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source()),
                        new TapPermanentsEffect(Subjects.source())
                ),
                "Sacrifice another creature: Rakdos gains indestructible until end of turn. Tap it. Activate only once each turn.",
                1
        ));
    }
}
