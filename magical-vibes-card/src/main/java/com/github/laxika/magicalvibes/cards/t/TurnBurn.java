package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Turn // Burn — a split card with fuse.
 * <p>
 * Turn {2}{U}: Until end of turn, target creature loses all abilities and becomes a red Weird with
 * base power and toughness 0/1.
 * Burn {1}{R}: Burn deals 2 damage to any target.
 * Fuse {3}{U}{R}: cast both halves as one spell, resolving Turn and then Burn (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). Turn's transformation
 * is one {@link SequenceEffect} so the fuse mode can declare one target filter per half; shared
 * targets are allowed because fusing both halves onto one creature is legal.
 */
@CardRegistration(set = "DGM", collectorNumber = "134")
public class TurnBurn extends Card {

    public TurnBurn() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target");

        CardEffect turn = SequenceEffect.of(
                new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN),
                new GrantColorUntilEndOfTurnEffect(CardColor.RED),
                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.WEIRD),
                new SetBasePowerToughnessEffect(0, 1));
        CardEffect burn = new DealDamageToAnyTargetEffect(2);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Turn — Until end of turn, target creature loses all abilities and becomes a red Weird with base power and toughness 0/1",
                        turn,
                        creature
                ).withManaCost("{2}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Burn — Deals 2 damage to any target",
                        burn,
                        anyTarget
                ).withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Turn and then Burn",
                        List.of(turn, burn),
                        List.of(creature, anyTarget)
                ).withManaCost("{3}{U}{R}")
        )));
    }
}
