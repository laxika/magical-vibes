package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "223")
public class JubilantSkybonder extends Card {

    public JubilantSkybonder() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                        new PermanentIsSourcePermanentPredicate(), 2, false),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                ))));
    }
}
