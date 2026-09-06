package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "247")
public class TophTheFirstMetalbender extends Card {

    public TophTheFirstMetalbender() {
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(
                CardType.LAND,
                GrantScope.OWN_PERMANENTS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                ))));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new EarthbendTargetLandEffect(2));
    }
}
