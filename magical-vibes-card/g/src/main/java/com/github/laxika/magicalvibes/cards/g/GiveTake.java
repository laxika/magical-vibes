package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

import java.util.List;

/**
 * Give // Take — a split card with fuse.
 * <p>
 * Give {2}{G}: Put three +1/+1 counters on target creature.
 * Take {2}{U}: Remove all +1/+1 counters from target creature you control. Draw that many cards.
 * Fuse {4}{G}{U}: cast both halves as one spell, resolving Give and then Take (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c).
 * <p>
 * Take is a "remove all" {@link RemoveCountersEffect}, a REMOVE_COUNTERS step the following
 * {@link DrawCardEffect} reads as "that many". The fuse mode declares one
 * filter per half so the two creatures are chosen independently — the modal unwrap binds effects to
 * filters positionally, so Give's counter effect takes group 0 and Take's removal group 1; the draw
 * takes no target. Shared targets are allowed because fusing onto a single creature (grow it, then
 * cash it in for cards) is a legal and central use of this card.
 */
@CardRegistration(set = "DGM", collectorNumber = "129")
@CardRegistration(set = "PIO", collectorNumber = "389")
public class GiveTake extends Card {

    public GiveTake() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        TargetFilter controlledCreature = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature you control");

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Give — Put three +1/+1 counters on target creature",
                        List.<CardEffect>of(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                        List.of(creature)
                ).withManaCost("{2}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Take — Remove all +1/+1 counters from target creature you control, then draw that many cards",
                        List.<CardEffect>of(
                                RemoveCountersEffect.allFromTarget(CounterType.PLUS_ONE_PLUS_ONE,
                                        new PermanentIsCreaturePredicate()),
                                new DrawCardEffect(new AmountThisWay(StepKind.REMOVE_COUNTERS))),
                        List.of(controlledCreature)
                ).withManaCost("{2}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Give and then Take",
                        List.<CardEffect>of(
                                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                                RemoveCountersEffect.allFromTarget(CounterType.PLUS_ONE_PLUS_ONE,
                                        new PermanentIsCreaturePredicate()),
                                new DrawCardEffect(new AmountThisWay(StepKind.REMOVE_COUNTERS))),
                        List.of(creature, controlledCreature)
                ).withManaCost("{4}{G}{U}")
        )));
    }
}
