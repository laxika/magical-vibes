package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "132")
public class FlowstoneThopter extends Card {

    public FlowstoneThopter() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new BoostSelfEffect(1, -1), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{1}: This creature gets +1/-1 and gains flying until end of turn."));
    }
}
