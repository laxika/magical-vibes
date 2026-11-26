package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "YBRO", collectorNumber = "13")
public class FoundryGroundbreaker extends Card {

    public FoundryGroundbreaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.you()),
                ConjureCardsEffect.named("BRO", "265", ConjureDestination.ontoBattlefieldTapped()).times(2)));
    }
}
