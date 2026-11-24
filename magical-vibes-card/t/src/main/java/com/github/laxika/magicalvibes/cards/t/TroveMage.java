package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "6")
public class TroveMage extends Card {

    public TroveMage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new SeekLibraryEffect(1, new CardTypePredicate(CardType.ARTIFACT)).fromAmongTop(10),
                        new ShuffleLibraryEffect(false)));
    }
}
