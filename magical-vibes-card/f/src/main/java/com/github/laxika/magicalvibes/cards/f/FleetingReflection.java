package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "49")
public class FleetingReflection extends Card {

    public FleetingReflection() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.target()))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(BecomeCopyOfSubjectEffect.of(Subjects.target(1)).by(Subjects.target(0)).untilEndOfTurn());
        target(TargetFilters.creature(), 0, 1);
    }
}
