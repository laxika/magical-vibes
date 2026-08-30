package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "FUT", collectorNumber = "107")
public class ScourgeOfKherRidges extends Card {

    public ScourgeOfKherRidges() {
        PermanentPredicate otherFlyingCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentHasKeywordPredicate(Keyword.FLYING)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new MassDamageEffect(2, false, false,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                "{1}{R}: This creature deals 2 damage to each creature without flying."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}",
                List.of(new MassDamageEffect(6, false, false, otherFlyingCreatures)),
                "{5}{R}: This creature deals 6 damage to each other creature with flying."
        ));
    }
}
