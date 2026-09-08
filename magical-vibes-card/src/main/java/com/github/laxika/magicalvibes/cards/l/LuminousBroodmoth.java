package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "21")
public class LuminousBroodmoth extends Card {

    public LuminousBroodmoth() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                new ReturnDyingCreatureToOwnerBattlefieldEffect(
                        CounterType.FLYING, 1, null, Set.of())));
    }
}
