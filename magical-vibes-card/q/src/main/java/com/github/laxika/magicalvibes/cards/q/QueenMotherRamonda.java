package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "MSC", collectorNumber = "23")
@CardRegistration(set = "MSC", collectorNumber = "320")
public class QueenMotherRamonda extends Card {

    public QueenMotherRamonda() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerIsMonarch(),
                new CreaturesCantAttackControllerUnlessPredicateEffect(
                        new PermanentPowerAtLeastPredicate(3))));
    }
}
