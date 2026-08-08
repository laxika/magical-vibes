package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Flesh // Blood — a split card with fuse.
 * <p>
 * Flesh {3}{B}{G}: Exile target creature card from a graveyard. Put X +1/+1 counters on target
 * creature, where X is the power of the card you exiled.
 * Blood {R}{G}: Target creature you control deals damage equal to its power to any target.
 * Fuse {3}{B}{G}{R}{G}: cast both halves as one spell, resolving Flesh and then Blood (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). Every mode declares
 * one target filter per target it takes.
 * <p>
 * Each mode lists its "any target" filter first because the modal unwrap binds the mode's effects to
 * its filters positionally, and only a bound effect whose {@code targetSpec()} admits players makes
 * a position player-targetable. Putting Blood's any-target slot at the position Blood's effect binds
 * to is what keeps the creature-only slots from accepting players. The effects read their real
 * groups from their declared group indices, so the declaration order is free.
 */
@CardRegistration(set = "DGM", collectorNumber = "128")
public class FleshBlood extends Card {

    public FleshBlood() {
        setAllowSharedTargets(true);

        TargetFilter creatureCardInGraveyard = new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS);
        TargetFilter creature = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature");
        TargetFilter controlledCreature = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature you control");
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Flesh — Exile target creature card from a graveyard, then put that many +1/+1 counters on target creature",
                        List.<CardEffect>of(new ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect(0, 1)),
                        List.of(creatureCardInGraveyard, creature)
                ).withManaCost("{3}{B}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Blood — Target creature you control deals damage equal to its power to any target",
                        List.<CardEffect>of(new TargetCreatureDealsPowerDamageToAnyTargetEffect(1, 0)),
                        List.of(anyTarget, controlledCreature)
                ).withManaCost("{R}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Flesh and then Blood",
                        List.<CardEffect>of(
                                new ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect(0, 2),
                                new TargetCreatureDealsPowerDamageToAnyTargetEffect(3, 1)),
                        List.of(creatureCardInGraveyard, anyTarget, creature, controlledCreature)
                ).withManaCost("{3}{B}{G}{R}{G}")
        )));
    }
}
