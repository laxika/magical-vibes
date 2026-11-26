package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "161")
public class GetALegUp extends Card {

    public GetALegUp() {
        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);

        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(creaturesYouControl, creaturesYouControl))
                .addSpellEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.target()));
    }
}
