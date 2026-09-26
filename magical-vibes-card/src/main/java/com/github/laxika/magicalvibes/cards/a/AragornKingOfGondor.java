package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LTC", collectorNumber = "5")
@CardRegistration(set = "LTC", collectorNumber = "85")
public class AragornKingOfGondor extends Card {

    public AragornKingOfGondor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());

        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(
                new ControllerIsMonarch(),
                new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES)));
    }
}
