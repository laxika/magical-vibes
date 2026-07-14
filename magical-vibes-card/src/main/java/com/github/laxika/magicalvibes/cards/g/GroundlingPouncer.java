package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "154")
public class GroundlingPouncer extends Card {

    public GroundlingPouncer() {
        addActivatedAbility(new ActivatedAbility(false, "{G/U}",
                List.of(new BoostSelfEffect(1, 3), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{G/U}: This creature gets +1/+3 and gains flying until end of turn. Activate only once each turn and only if an opponent controls a creature with flying.",
                null, null, 1, ActivationTimingRestriction.OPPONENT_CONTROLS_FLYING_CREATURE));
    }
}
