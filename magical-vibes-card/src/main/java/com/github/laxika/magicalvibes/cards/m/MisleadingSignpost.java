package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DuringDeclareAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "2127")
public class MisleadingSignpost extends Card {

    public MisleadingSignpost() {
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(
                        new DuringDeclareAttackers(),
                        new MayEffect(
                                new ReselectAttackingCreatureEffect(),
                                "Reselect which player or permanent the attacking creature is attacking?")));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
