package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "145")
@CardRegistration(set = "PIP", collectorNumber = "673")
@CardRegistration(set = "PIP", collectorNumber = "437")
@CardRegistration(set = "PIP", collectorNumber = "965")
public class T45PowerArmor extends Card {

    public T45PowerArmor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new PayEnergyCost(1),
                List.of(),
                true,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.ENCHANTED),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a menace counter on equipped creature",
                                        new PutCounterOnReferencedPermanentEffect(CounterType.MENACE)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a trample counter on equipped creature",
                                        new PutCounterOnReferencedPermanentEffect(CounterType.TRAMPLE)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a lifelink counter on equipped creature",
                                        new PutCounterOnReferencedPermanentEffect(CounterType.LIFELINK))
                        ))
                )));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
