package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "223")
public class HarbingerOfTheHunt extends Card {

    public HarbingerOfTheHunt() {
        PermanentPredicate withoutFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
        ));
        PermanentPredicate otherFlying = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new MassDamageEffect(1, false, false, withoutFlying)),
                "{2}{R}: This creature deals 1 damage to each creature without flying."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new MassDamageEffect(1, false, false, otherFlying)),
                "{2}{G}: This creature deals 1 damage to each other creature with flying."
        ));
    }
}
