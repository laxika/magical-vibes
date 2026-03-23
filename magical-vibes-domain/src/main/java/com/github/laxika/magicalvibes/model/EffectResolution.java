package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for computing targeting from a resolved effect list.
 * <p>
 * All targeting decisions should go through this class instead of asking Card directly.
 * Effects are "resolved" by unwrapping conditional wrappers (kicker, modal) based on
 * casting choices, then computing targeting from the concrete effects that will actually fire.
 */
public final class EffectResolution {

    private EffectResolution() {}

    /**
     * Resolves a raw effect list by unwrapping conditional wrappers based on casting choices.
     * <ul>
     *   <li>{@link KickerReplacementEffect}: resolves to {@code baseEffect} or {@code kickedEffect}</li>
     *   <li>{@link ChooseOneEffect}: resolves to the chosen mode's effect</li>
     *   <li>Other {@link com.github.laxika.magicalvibes.model.effect.ReplacementConditionalEffect}
     *       types (metalcraft, morbid, etc.): kept as-is because their condition depends on
     *       game state at resolution time, not casting time</li>
     * </ul>
     *
     * @param rawEffects the unresolved effect list from the card
     * @param kicked     whether the spell is kicked (null if unknown or not a kicker spell)
     * @param modeIndex  the chosen modal index for ChooseOneEffect (null if not modal)
     * @return the resolved effect list containing only the effects that will fire
     */
    public static List<CardEffect> resolveEffects(List<CardEffect> rawEffects, Boolean kicked, Integer modeIndex) {
        List<CardEffect> resolved = new ArrayList<>(rawEffects.size());
        for (CardEffect effect : rawEffects) {
            if (effect instanceof KickerReplacementEffect kre && kicked != null) {
                resolved.add(kicked ? kre.kickedEffect() : kre.baseEffect());
            } else if (effect instanceof ChooseOneEffect coe && modeIndex != null) {
                List<ChooseOneEffect.ChooseOneOption> options = coe.options();
                if (modeIndex >= 0 && modeIndex < options.size()) {
                    resolved.add(options.get(modeIndex).effect());
                } else {
                    resolved.add(effect);
                }
            } else {
                resolved.add(effect);
            }
        }
        return resolved;
    }

    /**
     * Computes the set of target types from a list of effects.
     *
     * @param spellEffects the SPELL slot effects (resolved or unresolved)
     * @param etbEffects   the ON_ENTER_BATTLEFIELD slot effects (may be empty)
     * @param isAura       whether the card is an aura
     * @param isEnchantPlayer whether the card is a player-enchanting aura (curse)
     * @return the set of target types these effects can target
     */
    public static Set<TargetType> computeAllowedTargets(List<CardEffect> spellEffects,
                                                         List<CardEffect> etbEffects,
                                                         boolean isAura,
                                                         boolean isEnchantPlayer) {
        Set<TargetType> result = EnumSet.noneOf(TargetType.class);
        if (isAura) {
            if (isEnchantPlayer) {
                result.add(TargetType.PLAYER);
            } else {
                result.add(TargetType.PERMANENT);
            }
        }
        for (CardEffect e : spellEffects) {
            collectTargetTypes(e, result);
        }
        for (CardEffect e : etbEffects) {
            if (e.canTargetPlayer()) result.add(TargetType.PLAYER);
            if (e.canTargetPermanent()) result.add(TargetType.PERMANENT);
        }
        return result;
    }

    /**
     * Returns true if the given effects require a non-spell target (player, permanent, graveyard, or exile).
     */
    public static boolean needsTarget(List<CardEffect> spellEffects,
                                       List<CardEffect> etbEffects,
                                       boolean isAura,
                                       boolean isEnchantPlayer) {
        Set<TargetType> t = computeAllowedTargets(spellEffects, etbEffects, isAura, isEnchantPlayer);
        return t.contains(TargetType.PLAYER) || t.contains(TargetType.PERMANENT)
                || t.contains(TargetType.GRAVEYARD) || t.contains(TargetType.EXILE);
    }

    /**
     * Returns true if the spell itself requires a target to be cast (MTG rule 601.2c).
     * Excludes ETB effects (separate from casting) and {@link CostEffect}s (not "targeting" in MTG terms).
     */
    public static boolean needsSpellCastTarget(List<CardEffect> spellEffects,
                                                boolean isAura,
                                                boolean isEnchantPlayer) {
        Set<TargetType> result = EnumSet.noneOf(TargetType.class);
        if (isAura) {
            if (isEnchantPlayer) {
                result.add(TargetType.PLAYER);
            } else {
                result.add(TargetType.PERMANENT);
            }
        }
        for (CardEffect e : spellEffects) {
            if (e instanceof CostEffect) continue;
            collectTargetTypes(e, result);
        }
        return result.contains(TargetType.PLAYER) || result.contains(TargetType.PERMANENT)
                || result.contains(TargetType.GRAVEYARD) || result.contains(TargetType.EXILE);
    }

    /**
     * Returns true if the given effects target a spell on the stack.
     */
    public static boolean needsSpellTarget(List<CardEffect> spellEffects) {
        return spellEffects.stream().anyMatch(CardEffect::canTargetSpell);
    }

    /**
     * Returns true if the given effects require damage distribution (divided damage spells).
     */
    public static boolean needsDamageDistribution(List<CardEffect> effects) {
        return effects.stream()
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect
                        || e instanceof DealDividedDamageAmongTargetCreaturesEffect);
    }

    // ===== Card convenience overloads (union semantics, no casting context) =====

    /**
     * Computes the set of target types from a card's effects (union of all possible targets).
     * Use this when no casting context (kicked, mode) is available.
     */
    public static Set<TargetType> computeAllowedTargets(Card card) {
        return computeAllowedTargets(
                card.getEffects(EffectSlot.SPELL),
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD),
                card.isAura(), card.isEnchantPlayer());
    }

    /**
     * Returns true if the card's effects require a non-spell target (union semantics).
     */
    public static boolean needsTarget(Card card) {
        return needsTarget(
                card.getEffects(EffectSlot.SPELL),
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD),
                card.isAura(), card.isEnchantPlayer());
    }

    /**
     * Returns true if the spell itself requires a target to be cast (MTG rule 601.2c, union semantics).
     */
    public static boolean needsSpellCastTarget(Card card) {
        return needsSpellCastTarget(card.getEffects(EffectSlot.SPELL), card.isAura(), card.isEnchantPlayer());
    }

    /**
     * Returns true if the card's effects target a spell on the stack (union semantics).
     */
    public static boolean needsSpellTarget(Card card) {
        return needsSpellTarget(card.getEffects(EffectSlot.SPELL));
    }

    /**
     * Returns true if the card's effects require damage distribution (union semantics).
     * Checks both spell effects and activated ability effects.
     */
    public static boolean needsDamageDistribution(Card card) {
        boolean inSpell = needsDamageDistribution(card.getEffects(EffectSlot.SPELL));
        boolean inAbility = card.getActivatedAbilities().stream()
                .flatMap(a -> a.getEffects().stream())
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect
                        || e instanceof DealDividedDamageAmongTargetCreaturesEffect);
        return inSpell || inAbility;
    }

    private static void collectTargetTypes(CardEffect e, Set<TargetType> out) {
        if (e.canTargetPlayer()) out.add(TargetType.PLAYER);
        if (e.canTargetPermanent()) out.add(TargetType.PERMANENT);
        if (e.canTargetSpell()) out.add(TargetType.SPELL_ON_STACK);
        if (e.canTargetGraveyard()) out.add(TargetType.GRAVEYARD);
        if (e.canTargetExile()) out.add(TargetType.EXILE);
    }
}
