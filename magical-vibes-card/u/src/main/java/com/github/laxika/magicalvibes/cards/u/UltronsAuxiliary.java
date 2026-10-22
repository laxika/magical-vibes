package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "669")
public class UltronsAuxiliary extends Card {

    public UltronsAuxiliary() {
        PutCountersOnSourceEffect counter = new PutCountersOnSourceEffect(1, 1, 1);
        addEffect(EffectSlot.ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        counter));
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD, counter);
    }
}
