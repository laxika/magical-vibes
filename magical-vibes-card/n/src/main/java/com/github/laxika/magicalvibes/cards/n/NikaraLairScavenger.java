package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "3")
public class NikaraLairScavenger extends Card {

    private static final String PARTNER_NAME = "Yannik, Scavenging Sentinel";

    public NikaraLairScavenger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put Yannik into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addTrigger(Triggers.leavesBattlefield(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasCountersPredicate(CounterType.ANY))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
