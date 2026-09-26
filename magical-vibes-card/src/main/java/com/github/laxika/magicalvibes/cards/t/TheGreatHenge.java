package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "161")
@CardRegistration(set = "TLE", collectorNumber = "41")
@CardRegistration(set = "CMM", collectorNumber = "294")
@CardRegistration(set = "CMM", collectorNumber = "562")
@CardRegistration(set = "LTC", collectorNumber = "348")
@CardRegistration(set = "LTC", collectorNumber = "378")
public class TheGreatHenge extends Card {

    public TheGreatHenge() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new GreatestPowerAmongControlled()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, 2), new GainLifeEffect(2)),
                "{T}: Add {G}{G}. You gain 2 life."
        ));

        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE),
                new DrawCardEffect()
        ));
    }
}
