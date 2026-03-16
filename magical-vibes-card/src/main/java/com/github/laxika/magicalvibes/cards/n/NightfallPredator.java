package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Nightfall Predator — back face of Daybreak Ranger.
 * 4/4 Werewolf.
 * {R}, {T}: Nightfall Predator fights target creature.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Nightfall Predator.
 */
public class NightfallPredator extends Card {

    public NightfallPredator() {
        // {R}, {T}: Nightfall Predator fights target creature.
        addActivatedAbility(new ActivatedAbility(
                true, "{R}",
                List.of(new SourceFightsTargetCreatureEffect()),
                "{R}, {T}: Nightfall Predator fights target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Nightfall Predator.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
