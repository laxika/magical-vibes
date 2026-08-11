package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "KTK", collectorNumber = "100")
public class BarrageOfBoulders extends Card {

    public BarrageOfBoulders() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(1, false, false,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES)));
    }
}
