package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "187")
@CardRegistration(set = "BRB", collectorNumber = "27")
public class FireAnts extends Card {

    public FireAnts() {
        PermanentPredicate otherNonFlyers = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MassDamageEffect(1, false, false, otherNonFlyers)),
                "{T}: This creature deals 1 damage to each other creature without flying."
        ));
    }
}
