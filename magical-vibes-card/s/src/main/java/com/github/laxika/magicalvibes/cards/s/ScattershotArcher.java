package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "90")
public class ScattershotArcher extends Card {

    public ScattershotArcher() {
        // {T}: This creature deals 1 damage to each creature with flying.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MassDamageEffect(1, false, false, new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "{T}: This creature deals 1 damage to each creature with flying."
        ));
    }
}
