package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "31")
public class ResistanceReunited extends Card {

    public ResistanceReunited() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2))
                .addSpellEffect(new GrantKeywordEffect(
                        Keyword.INDESTRUCTIBLE, Subjects.otherCreaturesYouControl(),
                        new PermanentIsEquippedPredicate()));
    }
}
