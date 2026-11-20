package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "59")
public class EchoingRuin extends Card {

    public EchoingRuin() {
        PermanentIsArtifactPredicate artifactPredicate = new PermanentIsArtifactPredicate();
        target(TargetFilters.artifact()).addSpellEffect(new DestroyTargetPermanentEffect(Subjects.allOf(Subjects.target(),
                Subjects.otherPermanentsSharing(Subjects.target(), SharedQuality.NAME, artifactPredicate))));
    }
}
