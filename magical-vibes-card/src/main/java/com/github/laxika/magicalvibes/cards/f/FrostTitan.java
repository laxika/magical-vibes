package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;

@CardRegistration(set = "M11", collectorNumber = "55")
public class FrostTitan extends Card {

    public FrostTitan() {
        // Whenever Frost Titan becomes the target of a spell or ability an opponent controls,
        // counter that spell or ability unless its controller pays {2}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        // Whenever Frost Titan enters the battlefield or attacks,
        // tap target permanent. It doesn't untap during its controller's next untap step.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapTargetPermanentEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SkipNextUntapOnTargetEffect());
        addEffect(EffectSlot.ON_ATTACK, new TapTargetPermanentEffect());
        addEffect(EffectSlot.ON_ATTACK, new SkipNextUntapOnTargetEffect());
    }
}
