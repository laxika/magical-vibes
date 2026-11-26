package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "174")
public class AshenFirebeast extends Card {

    public AshenFirebeast() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(DealDamageEffect.toEachCreature(1,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                "{1}{R}: This creature deals 1 damage to each creature without flying."
        ));
    }
}
