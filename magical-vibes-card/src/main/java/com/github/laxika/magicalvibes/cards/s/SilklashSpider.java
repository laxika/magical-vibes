package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "271")
public class SilklashSpider extends Card {

    public SilklashSpider() {
        // {X}{G}{G}: This creature deals X damage to each creature with flying.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{G}{G}",
                List.of(new MassDamageEffect(0, true, false, new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "{X}{G}{G}: This creature deals X damage to each creature with flying."
        ));
    }
}
