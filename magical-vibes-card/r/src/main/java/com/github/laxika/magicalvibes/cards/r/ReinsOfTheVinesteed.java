package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "134")
public class ReinsOfTheVinesteed extends Card {

    public ReinsOfTheVinesteed() {
        target(TargetFilters.creature())
                // Enchanted creature gets +2/+2.
                .addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()))

                // When enchanted creature dies, you may return this card from your graveyard to the
                // battlefield attached to a creature that shares a creature type with that creature.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.inYourGraveyard(Subjects.sourceCard()))
                                .underControlOf(Subjects.you())
                                .attachedTo(Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentSharesCreatureTypeWithTriggeringPermanentPredicate())))),
                                "return it to the battlefield attached to a creature that shares a creature type"
                                        + " with the creature that died?"));
    }
}
