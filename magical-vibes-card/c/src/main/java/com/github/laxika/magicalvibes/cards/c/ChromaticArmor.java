package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "283")
public class ChromaticArmor extends Card {

    public ChromaticArmor() {
        target(TargetFilters.creature()).addStaticEffect(
                PreventDamageToRecipientsEffect.to(Subjects.attachedCreature()).fromSourcesOfChosenColor());
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(new EnterWithCountersEffect(CounterType.SLEIGHT, new Fixed(1)));

        // "{X}: Put a sleight counter on this Aura and choose a color. X is the number of sleight
        // counters on this Aura." X is not chosen by the player — the printed cost is {0} and the
        // per-counter increase supplies the generic mana, read at activation time.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new IncreaseActivationCostEffect(new CountersOnSource(CounterType.SLEIGHT)),
                        PutCountersEffect.onSelf(CounterType.SLEIGHT),
                        new ChooseColorForSourceEffect()),
                "{X}: Put a sleight counter on this Aura and choose a color. X is the number of sleight counters on this Aura."));
    }
}
