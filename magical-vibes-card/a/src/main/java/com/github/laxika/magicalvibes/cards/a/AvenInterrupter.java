package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlotExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "4")
public class AvenInterrupter extends Card {

    public AvenInterrupter() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTruePredicate(), "Target must be a spell."))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                        new ExileTargetSpellEffect(),
                        new PlotExiledCardsEffect(Subjects.cardsThisWay(StepKind.EXILE))));
        addStaticEffect(new IncreaseSpellCostEffect(
                new CardTruePredicate(), 2, CostModificationScope.OPPONENT,
                Set.of(Zone.GRAVEYARD, Zone.EXILE)));
    }
}
