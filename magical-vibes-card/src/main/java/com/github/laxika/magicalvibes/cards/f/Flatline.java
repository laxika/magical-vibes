package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "WHO", collectorNumber = "43")
@CardRegistration(set = "WHO", collectorNumber = "358")
public class Flatline extends Card {

    public Flatline() {
        addEffect(EffectSlot.SPELL,
                new SetBasePowerToughnessEffect(0, 1, GrantScope.OPPONENT_CREATURES));
    }
}
