package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "248")
@CardRegistration(set = "SLD", collectorNumber = "492")
@CardRegistration(set = "SLD", collectorNumber = "699")
@CardRegistration(set = "SIR", collectorNumber = "239")
public class OliviaMobilizedForWar extends Card {

    public OliviaMobilizedForWar() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(
                        SequenceEffect.of(
                                new DiscardEffect(1, Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.discarded(),
                                        SequenceEffect.of(
                                                new PutCountersEffect(
                                                        Subjects.triggering(), CounterType.PLUS_ONE_PLUS_ONE),
                                                new GrantKeywordEffect(Keyword.HASTE, Subjects.triggering()),
                                                new GrantSubtypeEffect(CardSubtype.VAMPIRE, Subjects.triggering())))),
                        "Discard a card to put a +1/+1 counter on that creature, give it haste, and make it a Vampire?"));
    }
}
