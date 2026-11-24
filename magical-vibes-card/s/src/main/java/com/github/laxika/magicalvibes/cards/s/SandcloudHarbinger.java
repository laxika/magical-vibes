package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "3")
public class SandcloudHarbinger extends Card {

    public SandcloudHarbinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ConjureCardsEffect.named("AKH", "249", ConjureDestination.intoTopCardsOfLibraryAtRandom(10))
                        .times(3)
                        .into(Subjects.eachPlayer()));

        CardSubtypePredicate desert = new CardSubtypePredicate(CardSubtype.DESERT);
        addTrigger(Triggers.youPlayLand().withFilter(desert),
                SequenceEffect.of(new DrawCardEffect(1), new GainLifeEffect(3)));
        addTrigger(Triggers.opponentPlaysLand().withFilter(desert),
                SequenceEffect.of(new DrawCardEffect(1, Subjects.triggeringPlayer()), new GainLifeEffect(3)));
    }
}
