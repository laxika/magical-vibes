package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "IKO", collectorNumber = "230")
public class SonorousHowlbonder extends Card {

    public SonorousHowlbonder() {
        addStaticEffect(new GrantNonAbilityEffectEffect(
                new CantBeBlockedByFewerThanNCreaturesEffect(3),
                Subjects.creaturesYouControl(),
                new PermanentHasKeywordPredicate(Keyword.MENACE)));
    }
}
