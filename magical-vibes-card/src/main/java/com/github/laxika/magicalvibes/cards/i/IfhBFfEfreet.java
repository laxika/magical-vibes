package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "ME1", collectorNumber = "123")
public class IfhBFfEfreet extends Card {

    public IfhBFfEfreet() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new MassDamageEffect(1, false, true,
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "{G}: This creature deals 1 damage to each creature with flying and each player. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
