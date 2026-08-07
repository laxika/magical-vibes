package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "TMP", collectorNumber = "214")
public class ApesOfRath extends Card {

    public ApesOfRath() {
        // Whenever this creature attacks, it doesn't untap during its controller's next untap step.
        addEffect(EffectSlot.ON_ATTACK, new SkipNextUntapEffect(TapUntapScope.SELF));
    }
}
