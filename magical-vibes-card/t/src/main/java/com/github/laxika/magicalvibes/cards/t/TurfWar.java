package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandAndPutCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TurfWarCombatDamageEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "54")
@CardRegistration(set = "NCC", collectorNumber = "154")
public class TurfWar extends Card {

    private static final PermanentPredicate CONTESTED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasCountersPredicate(CounterType.CONTESTED)));

    public TurfWar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerChoosesLandAndPutCounterEffect(CounterType.CONTESTED));
        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
                new ConditionalEffect(new ControlsPermanent(CONTESTED_LAND),
                        new TurfWarCombatDamageEffect(true), true));
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT,
                new ConditionalEffect(new TargetPlayerControlsPermanent(CONTESTED_LAND),
                        new TurfWarCombatDamageEffect(false), true));
    }
}
