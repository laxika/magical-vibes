package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "30")
@CardRegistration(set = "SOC", collectorNumber = "171")
public class ShelteredByGhosts extends Card {

    public ShelteredByGhosts() {
        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                        UnlessPaysEffect.counterUnlessPays(2)), Subjects.attachedCreature()));

        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
