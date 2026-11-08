package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CasualtyCostPaid;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtNoncombatDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "82")
public class GrislySigil extends Card {

    public GrislySigil() {
        addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new CasualtyCostPaid()),
                new CopyTriggeringSpellEffect());
        addCastingCost(new CasualtyCost(1));

        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()
        ));

        target(new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker,
                "Target must be a creature or planeswalker"
        )).addSpellEffect(new ConditionalReplacementEffect(
                new TargetPermanentMatches(new PermanentDealtNoncombatDamageThisTurnPredicate()),
                SequenceEffect.of(
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(1),
                        new GainLifeEffect(1)
                ),
                SequenceEffect.of(
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(3),
                        new GainLifeEffect(3)
                )
        ));
    }
}
