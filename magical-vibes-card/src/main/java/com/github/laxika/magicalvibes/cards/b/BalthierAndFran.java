package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedVehicleAttackEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "213")
public class BalthierAndFran extends Card {

    public BalthierAndFran() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, Set.of(Keyword.REACH, Keyword.VIGILANCE), GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addEffect(EffectSlot.ON_CREWS_VEHICLE, new RegisterDelayedVehicleAttackEffect(
                new MayPayManaEffect("{1}{R}{G}", new AdditionalCombatPhaseEffect(1),
                        "Pay {1}{R}{G} to get an additional combat phase?")));
    }
}
