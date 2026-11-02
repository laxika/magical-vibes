package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DuringDeclareAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2127")
@CardRegistration(set = "WOC", collectorNumber = "11")
@CardRegistration(set = "WOC", collectorNumber = "47")
@CardRegistration(set = "FDC", collectorNumber = "69")
public class MisleadingSignpost extends Card {

    public MisleadingSignpost() {
        target(TargetFilters.attackingCreature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(
                        new DuringDeclareAttackers(),
                        new MayEffect(
                                new ReselectAttackingCreatureEffect(),
                                "Reselect which player or permanent the attacking creature is attacking?")));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
