package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "41")
@CardRegistration(set = "HA7", collectorNumber = "7")
public class EchoingDecay extends Card {

    public EchoingDecay() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.each(
                Subjects.allOf(Subjects.target(), Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.NAME)),
                -2, -2));
    }
}
