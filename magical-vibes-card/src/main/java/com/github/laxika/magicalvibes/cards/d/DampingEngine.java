package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMorePermanentsThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.DampingEngineEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreSourceDampingEngineEffectUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "124")
public class DampingEngine extends Card {

    public DampingEngine() {
        addEffect(EffectSlot.STATIC, new DampingEngineEffect());
        addActivatedAbility(new ActivatedAbility(false, null,
                        List.of(new SacrificePermanentCost(new PermanentTruePredicate(), "a permanent", false),
                                new IgnoreSourceDampingEngineEffectUntilEndOfTurnEffect()),
                        "Sacrifice a permanent: Ignore this effect until end of turn.", 1)
                .withActivationCondition(new ControllerControlsMorePermanentsThanEachOtherPlayer(),
                        "Activate only if you control more permanents than each other player.")
                .withActivatableByAnyPlayer());
    }
}
