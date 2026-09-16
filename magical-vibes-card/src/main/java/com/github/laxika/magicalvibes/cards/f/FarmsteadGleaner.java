package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "222")
public class FarmsteadGleaner extends Card {

    public FarmsteadGleaner() {
        // This creature doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // {2}, {Q}: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new PutCountersOnSourceEffect(1, 1, 1)),
                "{2}, {Q}: Put a +1/+1 counter on Farmstead Gleaner."
        ).withRequiresUntap());
    }
}
