package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2452")
public class T45PowerArmor extends Card {

    public T45PowerArmor() {
        // When this Equipment enters, you get two energy counters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));

        // Equipped creature gets +3/+3 and doesn't untap during its controller's untap step.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());

        // At the beginning of your upkeep, you may pay one energy counter. If you do, untap
        // equipped creature, then put your choice of a menace, trample, or lifelink counter on it.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new PayEnergyCost(1),
                List.of(),
                true,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.ENCHANTED),
                        chooseKeywordCounter())));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }

    private static ChooseOneEffect chooseKeywordCounter() {
        return new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a menace counter on equipped creature",
                        new PutCounterOnReferencedPermanentEffect(CounterType.MENACE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a trample counter on equipped creature",
                        new PutCounterOnReferencedPermanentEffect(CounterType.TRAMPLE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a lifelink counter on equipped creature",
                        new PutCounterOnReferencedPermanentEffect(CounterType.LIFELINK))));
    }
}
