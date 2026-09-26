package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceByEnteringPowerEffect;

@CardRegistration(set = "YDFT", collectorNumber = "24")
public class QuickbeastAmulet extends Card {

    public QuickbeastAmulet() {
        CountersOnSource intensity = new CountersOnSource(CounterType.INTENSITY);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                intensity, intensity, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new IntensifySourceByEnteringPowerEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
