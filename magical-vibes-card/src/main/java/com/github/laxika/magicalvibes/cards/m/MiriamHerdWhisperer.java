package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "221")
public class MiriamHerdWhisperer extends Card {

    public MiriamHerdWhisperer() {
        Set<CardSubtype> mountsAndVehicles = Set.of(CardSubtype.MOUNT, CardSubtype.VEHICLE);

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(
                        Keyword.HEXPROOF,
                        GrantScope.OWN_PERMANENTS,
                        new PermanentHasAnySubtypePredicate(mountsAndVehicles))));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasAnySubtypePredicate(mountsAndVehicles),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)));
    }
}
