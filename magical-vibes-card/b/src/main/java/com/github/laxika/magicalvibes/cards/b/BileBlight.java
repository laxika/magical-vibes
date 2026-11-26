package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "61")
@CardRegistration(set = "PIO", collectorNumber = "369")
@CardRegistration(set = "40K", collectorNumber = "195")
public class BileBlight extends Card {

    public BileBlight() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.each(
                Subjects.allOf(Subjects.target(), Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.NAME)),
                -3, -3));
    }
}
