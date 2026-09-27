package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DuringDeclareAttackers;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureAttackTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CMM", collectorNumber = "112")
public class PortalMage extends Card {

    public PortalMage() {
        CardEffect ability = new ConditionalEffect(
                new DuringDeclareAttackers(),
                new MayEffect(
                        new ReselectAttackingCreatureAttackTargetEffect(),
                        "Reselect the attacking creature's attack target?"));
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ability);
    }
}
