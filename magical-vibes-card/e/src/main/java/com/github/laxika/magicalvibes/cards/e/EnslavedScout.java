package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "71a")
@CardRegistration(set = "ALL", collectorNumber = "71b")
public class EnslavedScout extends Card {

    public EnslavedScout() {
        // {2}: This creature gains mountainwalk until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantKeywordEffect(Keyword.MOUNTAINWALK, GrantScope.SELF)),
                "{2}: This creature gains mountainwalk until end of turn."
        ));
    }
}
