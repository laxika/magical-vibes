package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "139")
public class ChainsOfCustody extends Card {

    public ChainsOfCustody() {
        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                        UnlessPaysEffect.counterUnlessPays(2)), Subjects.attachedCreature()));

        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
