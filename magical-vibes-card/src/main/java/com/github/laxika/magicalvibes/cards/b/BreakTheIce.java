package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCouldProduceManaPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Destroys a land that is snow or could produce colorless mana.
 *
 * <p>Overload changes the targeted destruction into destruction of every matching land.</p>
 */
@CardRegistration(set = "MH2", collectorNumber = "77")
public class BreakTheIce extends Card {

    public BreakTheIce() {
        PermanentPredicate qualifyingLand = qualifyingLand();
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{B}{B}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new DestroyTargetPermanentEffect(qualifyingLand),
                new DestroyAllPermanentsEffect(qualifyingLand)));
        target(new PermanentPredicateTargetFilter(qualifyingLand,
                "Target must be a snow land or a land that could produce colorless mana"));
    }

    private static PermanentPredicate qualifyingLand() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                        new PermanentCouldProduceManaPredicate(ManaColor.COLORLESS)))));
    }
}
