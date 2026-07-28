package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivationCostPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventColorDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "283")
public class ChromaticArmor extends Card {

    public ChromaticArmor() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new PreventColorDamageToEnchantedCreatureEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.SLEIGHT, new Fixed(1)));

        // "{X}: Put a sleight counter on this Aura and choose a color. X is the number of sleight
        // counters on this Aura." X is not chosen by the player — the printed cost is {0} and the
        // per-counter increase supplies the generic mana, read at activation time.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new IncreaseActivationCostPerCounterEffect(CounterType.SLEIGHT, 1),
                        new PutCountersOnSelfEffect(CounterType.SLEIGHT),
                        new ChooseColorForSourceEffect()),
                "{X}: Put a sleight counter on this Aura and choose a color. X is the number of sleight counters on this Aura."));
    }
}
