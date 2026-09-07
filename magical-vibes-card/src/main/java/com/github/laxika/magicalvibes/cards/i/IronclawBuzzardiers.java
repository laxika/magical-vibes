package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerAtLeastEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "165")
public class IronclawBuzzardiers extends Card {

    public IronclawBuzzardiers() {
        addEffect(EffectSlot.STATIC, new CantBlockCreaturesWithPowerAtLeastEffect(2));
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{R}: Ironclaw Buzzardiers gains flying until end of turn."));
    }
}
