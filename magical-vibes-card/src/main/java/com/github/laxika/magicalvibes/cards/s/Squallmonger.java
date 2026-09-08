package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "276")
public class Squallmonger extends Card {

    public Squallmonger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new MassDamageEffect(1, false, true,
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "{2}: This creature deals 1 damage to each creature with flying and each player. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
