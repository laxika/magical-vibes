package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "165")
public class ResurrectionOrb extends Card {

    public ResurrectionOrb() {
        // Equipped creature has lifelink.
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, return that card under its owner's control at the
        // beginning of the next end step.
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED),
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true));

        // Equip {4}.
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
