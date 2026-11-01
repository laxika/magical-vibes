package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
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
                        DiscardCardThenEffect.continuing(
                                null,
                                SequenceEffect.of(
                                        new PutCounterOnReferencedPermanentEffect(
                                                PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE),
                                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT),
                                        new GrantSubtypeToTriggeringPermanentEffect(CardSubtype.VAMPIRE)),
                                "a card"),
                        "Discard a card to put a +1/+1 counter on that creature, give it haste, and make it a Vampire?"));
    }
}
