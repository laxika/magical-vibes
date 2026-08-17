package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByConvergeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.amount.ColorManaPairsSpentToCast;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
     *   <li>Kicker {@link ConditionalReplacementEffect}: resolves to the base or upgraded effect</li>
     *   <li>{@link ChooseOneEffect}: resolves to the chosen mode's effect</li>
     *   <li>Other {@link ConditionalReplacementEffect} conditions (metalcraft, morbid, etc.):
     *       kept as-is because their condition depends on game state at resolution time,
     *       not casting time</li>
     * </ul>
     *
     * @param rawEffects the unresolved effect list from the card
     * @param kicked     whether the spell is kicked (null if unknown or not a kicker spell)
     * @param modeIndex  the chosen modal index for ChooseOneEffect (null if not modal)
     * @return the resolved effect list containing only the effects that will fire
     */
    public static List<CardEffect> resolveEffects(List<CardEffect> rawEffects, Boolean kicked, Integer modeIndex) {
        return resolveEffects(rawEffects, kicked, null, modeIndex);
    }

    /**
     * As {@link #resolveEffects(List, Boolean, Integer)}, but also unwraps the overload text change
     * (CR 702.96a): an {@code Overloaded} {@link ConditionalReplacementEffect} resolves to its
     * upgraded ("each") branch when the spell was cast for its overload cost and to the printed
     * ("target") branch otherwise. Because the two branches differ in target shape — CR 702.96b says
     * an overloaded spell has no targets — the cast path must resolve with this flag before asking
     * whether a target is required.
     *
     * @param overloaded whether the spell is being cast for its overload cost (null if unknown)
     */
    public static List<CardEffect> resolveEffects(List<CardEffect> rawEffects, Boolean kicked,
                                                  Boolean overloaded, Integer modeIndex) {
        List<CardEffect> resolved = new ArrayList<>(rawEffects.size());
        for (CardEffect effect : rawEffects) {
            if (effect instanceof ConditionalReplacementEffect cre
                    && cre.condition() instanceof Overloaded && overloaded != null) {
                resolved.add(overloaded ? cre.upgradedEffect() : cre.baseEffect());
            } else if (effect instanceof ConditionalReplacementEffect cre
                    && cre.condition() instanceof Kicked && kicked != null) {
                resolved.add(kicked ? cre.upgradedEffect() : cre.baseEffect());
            } else if (effect instanceof ConditionalEffect ce
                    && ce.condition() instanceof Kicked && kicked != null) {
                if (kicked) {
                    resolved.add(ce.wrapped());
                }
            } else if (effect instanceof ChooseOneEffect coe && modeIndex != null) {
                if (coe.choicesRequired() == 1 && coe.choicesMax() == 1) {
                    List<ChooseOneEffect.ChooseOneOption> options = coe.options();
                    if (modeIndex >= 0 && modeIndex < options.size()) {
                        resolved.addAll(options.get(modeIndex).effects());
                    } else {
                        resolved.add(effect);
                    }
                } else if (modeIndex < 0) {
                    for (int chosenModeIndex : coe.decodeModeIndices(modeIndex)) {
                        resolved.addAll(coe.options().get(chosenModeIndex).effects());
                    }
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
            } else if (!enchantsGraveyardCard(spellEffects)) {
                result.add(TargetType.PERMANENT);
            }
        }
        for (CardEffect e : spellEffects) {
            collectTargetTypes(e, result);
        }
        for (CardEffect e : etbEffects) {
            // A "may" ETB ability (e.g. Leonin Relic-Warder's "you may exile target
            // artifact or enchantment") chooses its target when the trigger is put on the
            // stack after the permanent enters (CR 603.3d), never at cast time. It must not
            // make the spell report a cast-time target requirement — otherwise the permanent
            // couldn't be cast when no legal target exists, which is illegal (CR 601.2c only
            // the spell's own targets gate casting). The trigger still resolves its own
            // targeting after entry, doing nothing when there are no legal targets.
            if (e instanceof MayEffect) continue;
            // "You may pay {X}. If you don't, [targeted effect]" (Knight of the Mists) — and the
            // "if you do" MayPayMana shape — choose targets as the trigger goes on the stack
            // after the permanent enters (CR 603.3d), never at cast time. Cast-time targeting
            // would miss the entering permanent itself as a legal target.
            if (e instanceof MayPayManaEffect) continue;
            // A gate-conditional ETB ("Metalcraft — When ~ enters, ... target player loses
            // 4 life") is an intervening-if trigger (CR 603.4): whether it triggers at all
            // depends on game state as the permanent enters, so the target can't be a
            // cast-time requirement. The target is chosen as the trigger goes on the stack
            // (CR 603.3d) via the ETBTokenTargetTrigger path — and never chosen at all when
            // the gate isn't met.
            if (e instanceof ConditionalEffect ce && ce.condition().isEtbTriggerGate()) continue;
            TargetSpec spec = e.targetSpec();
            if (spec.admits(TargetPredicate.Kind.PLAYER)) result.add(TargetType.PLAYER);
            if (spec.admits(TargetPredicate.Kind.PERMANENT)) result.add(TargetType.PERMANENT);
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
            } else if (!enchantsGraveyardCard(spellEffects)) {
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
        return spellEffects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
    }

    /**
     * Whether the effect can target a spell on the stack — the successor to the deleted
     * {@code CardEffect.canTargetSpell()}. Almost every effect answers this from its
     * {@link CardEffect#targetSpec()} declaring a {@link TargetPredicate.Kind#SPELL} leaf.
     * Dual spell-or-permanent effects ({@link ChangeColorTextEffect}, {@link SetTargetColorEffect},
     * {@link PutTargetSpellOrPermanentIntoLibraryNFromTopEffect}) ALSO target a spell independently
     * of their permanent spec; spell targets are validated on the stack path, never by the spec
     * interpreter.
     */
    public static boolean targetsSpellOnStack(CardEffect e) {
        return e.targetSpec().admits(TargetPredicate.Kind.SPELL)
                || (e instanceof ChangeColorTextEffect c && c.canTargetSpell())
                || e instanceof SetTargetColorEffect
                || (e instanceof GrantColorUntilEndOfTurnEffect c && c.canTargetSpell())
                || e instanceof PutTargetSpellOrPermanentIntoLibraryNFromTopEffect;
    }

    /**
     * The permanent-target predicate for the effect — the successor to the deleted
     * {@code CardEffect.targetPredicate()}. Almost every effect carries it on its
     * {@link CardEffect#targetSpec()} predicate. {@link PutCounterOnTargetPermanentEffect} is the one
     * case that keeps its targeting restriction on a dedicated {@code targetPredicate} record
     * component (its spec predicate stays {@code null} to avoid a cast-time gate); the saga-chapter
     * and end-step targeting pipelines read the restriction through this component.
     */
    public static PermanentPredicate targetPredicateOf(CardEffect e) {
        return e instanceof PutCounterOnTargetPermanentEffect p
                ? p.targetPredicate()
                : e.targetSpec().predicate();
    }

    /**
     * The restriction a target slot that carries no {@code TargetFilter} of its own inherits from
     * the effects that will use it: every permanent restriction those effects declare, conjoined.
     * Empty when none of them declares one — a bare {@code PERMANENT} spec, and one that accepts
     * every player <em>and</em> every permanent, say nothing about which permanent is legal.
     *
     * <p>This is what "any target" (CR 115.4 — a creature, player, planeswalker or battle) is
     * expressed by now. It used to be <em>inferred</em> in target enumeration, from every
     * permanent-targeting effect also accepting players; that test could not tell "any target"
     * apart from "a player or any permanent", so the effects that declared the latter as an
     * unchecked escape hatch silently received the former's creature/planeswalker narrowing while
     * their real restriction ("among any number of target creatures") went unexpressed. See
     * {@code agent-docs/TARGET_PREDICATE_PLAN.md}, Step 2b.</p>
     */
    public static Optional<PermanentPredicate> declaredPermanentRestriction(List<CardEffect> effects) {
        List<PermanentPredicate> declared = new ArrayList<>();
        for (CardEffect e : effects) {
            TargetPredicate predicate = e.targetSpec().targetPredicate();
            if (predicate == null) {
                continue;
            }
            PermanentPredicate restriction = predicate.permanentRestriction().orElse(null);
            if (restriction == null || restriction instanceof PermanentTruePredicate) {
                continue;
            }
            declared.add(restriction);
        }
        if (declared.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(declared.size() == 1 ? declared.getFirst() : new PermanentAllOfPredicate(declared));
    }

    /**
     * Whether a player may be chosen for a target slot that carries no {@code TargetFilter} of its
     * own — true when some effect that will use the slot accepts one. Mirrors
     * {@code Card.doesPositionAllowPlayerTargets}, which asks the same question of a spell's
     * declared target groups.
     */
    public static boolean allowsPlayerTargets(List<CardEffect> effects) {
        return effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
    }

    /**
     * Returns true if the given effects require damage distribution (divided damage spells).
     */
    public static boolean needsDamageDistribution(List<CardEffect> effects) {
        return effects.stream().anyMatch(EffectResolution::distributesAmountsAmongTargets);
    }

    /**
     * An effect whose per-target integer amounts are announced by the controller (CR 601.2d) and
     * carried on {@code StackEntry.damageAssignments}: divided damage (CHOSEN mode reading the
     * standard targeting buffer, not the ETB {@code pendingETBDamageAssignments} path), divided
     * prevention (Remedy) or a chosen counter distribution (Spoils of War).
     *
     * <p>Its targets live in that assignment map rather than in the stack entry's single
     * {@code targetId}, so the target-validation pipeline must not demand a {@code targetId} for
     * it. {@code TargetValidationService} reads this instead of inferring the tolerance from the
     * spec's shape, which is what let these effects declare a deliberately no-op target
     * ({@code playerOrPermanent()}) and hide what they really target.
     */
    public static boolean distributesAmountsAmongTargets(CardEffect e) {
        return isChosenDivision(e)
                || e instanceof PreventDividedDamageEffect
                || (e instanceof DistributeCountersAmongTargetsEffect d && d.mode() == DivisionMode.CHOSEN);
    }

    private static boolean isChosenDivision(CardEffect e) {
        return e instanceof DealDividedDamageEffect d
                && d.mode() == DivisionMode.CHOSEN && !d.etbAssignments();
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
                .anyMatch(EffectResolution::isChosenDivision);
        return inSpell || inAbility;
    }

    /**
     * Returns true if any of the given effects use the Converge mechanic.
     */
    public static boolean hasConvergeEffect(List<CardEffect> effects) {
        return effects.stream().anyMatch(TargetPlayerDiscardsByConvergeEffect.class::isInstance);
    }

    /**
     * Returns true if the card uses Converge or Sunburst (keyword or converge-scaling spell effect).
     */
    public static boolean hasConvergeEffect(Card card) {
        return (card.getKeywords().contains(Keyword.CONVERGE)
                || card.getKeywords().contains(Keyword.SUNBURST))
                || hasConvergeEffect(card.getEffects(EffectSlot.SPELL));
    }

    /**
     * Returns true when an enter-with-counters effect gets its X value from distinct colors spent
     * to cast the permanent.
     */
    public static boolean hasColorsSpentCounterEffect(Card card) {
        return card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(effect -> {
                    if (effect instanceof EnterWithCountersEffect enterWith) {
                        return enterWith.countColorsSpent();
                    }
                    return effect instanceof ConditionalEffect conditional
                            && conditional.wrapped() instanceof EnterWithCountersEffect wrapped
                            && wrapped.countColorsSpent();
                });
    }

    /**
     * Returns true if a spell or its battlefield-entry effect is gated on which mana color was
     * spent to cast it ({@link com.github.laxika.magicalvibes.model.condition.ColorSpentToCast}).
     * Signals that spell payment must snapshot the colors and amounts of mana spent (e.g. Repel
     * Intruders and Catharsis).
     */
    public static boolean hasColorSpentCondition(Card card) {
        return java.util.stream.Stream.of(EffectSlot.SPELL, EffectSlot.ON_ENTER_BATTLEFIELD)
                .flatMap(slot -> card.getEffects(slot).stream())
                .anyMatch(e -> e instanceof ConditionalEffect c
                        && conditionUsesColorSpentMana(c.condition()));
    }

    private static boolean conditionUsesColorSpentMana(Condition condition) {
        return switch (condition) {
            case ColorSpentToCast ignored -> true;
            case AllConditions c -> c.conditions().stream().anyMatch(EffectResolution::conditionUsesColorSpentMana);
            case AnyOf c -> c.conditions().stream().anyMatch(EffectResolution::conditionUsesColorSpentMana);
            case NotCondition c -> conditionUsesColorSpentMana(c.inner());
            default -> false;
        };
    }

    /**
     * Returns true if a spell or its battlefield-entry effect reads the number of complete pairs of
     * one color spent to cast it. The cast path must retain the per-color payment snapshot until
     * the permanent's entry replacements and entry triggers have resolved.
     */
    public static boolean hasColorManaPairsSpentToCastAmount(Card card) {
        return java.util.stream.Stream.of(EffectSlot.SPELL, EffectSlot.ON_ENTER_BATTLEFIELD)
                .flatMap(slot -> card.getEffects(slot).stream())
                .anyMatch(EffectResolution::effectUsesColorManaPairsSpentToCast);
    }

    private static boolean effectUsesColorManaPairsSpentToCast(CardEffect effect) {
        if (effect instanceof EnterWithCountersEffect enter) {
            return amountUsesColorManaPairsSpentToCast(enter.count());
        }
        if (effect instanceof PutCounterOnTargetPermanentEffect putCounter) {
            return amountUsesColorManaPairsSpentToCast(putCounter.amount());
        }
        if (effect instanceof ConditionalEffect conditional) {
            return effectUsesColorManaPairsSpentToCast(conditional.wrapped());
        }
        return false;
    }

    private static boolean amountUsesColorManaPairsSpentToCast(DynamicAmount amount) {
        if (amount instanceof ColorManaPairsSpentToCast) return true;
        if (amount instanceof Scaled scaled) {
            return amountUsesColorManaPairsSpentToCast(scaled.amount());
        }
        if (amount instanceof Divided divided) {
            return amountUsesColorManaPairsSpentToCast(divided.amount());
        }
        if (amount instanceof HalvedRoundedUp halved) {
            return amountUsesColorManaPairsSpentToCast(halved.amount());
        }
        if (amount instanceof Sum sum) {
            return sum.amounts().stream().anyMatch(EffectResolution::amountUsesColorManaPairsSpentToCast);
        }
        if (amount instanceof Min min) {
            return min.amounts().stream().anyMatch(EffectResolution::amountUsesColorManaPairsSpentToCast);
        }
        if (amount instanceof Max max) {
            return max.amounts().stream().anyMatch(EffectResolution::amountUsesColorManaPairsSpentToCast);
        }
        return false;
    }

    public static boolean hasManaSpentToCastDamageEffect(List<CardEffect> effects) {
        return hasManaSpentToCastAmount(effects);
    }

    public static boolean hasManaSpentToCastDamageEffect(Card card) {
        return hasManaSpentToCastAmount(card);
    }

    /**
     * True when any spell effect reads {@link ManaSpentToCast} — the cast path must snapshot total
     * mana spent into the stack entry's {@code xValue} (Molten Note damage; Memory Deluge look count).
     */
    public static boolean hasManaSpentToCastAmount(Card card) {
        return hasManaSpentToCastAmount(card.getEffects(EffectSlot.SPELL));
    }

    public static boolean hasManaSpentToCastAmount(List<CardEffect> effects) {
        return effects.stream().anyMatch(EffectResolution::effectUsesManaSpentToCast);
    }

    private static boolean effectUsesManaSpentToCast(CardEffect e) {
        if (e instanceof DealDamageToTargetCreatureEffect d) {
            return d.damage() instanceof ManaSpentToCast;
        }
        if (e instanceof LookAtTopCardsEffect look) {
            return look.lookCount() instanceof ManaSpentToCast
                    || look.chooseCount() instanceof ManaSpentToCast;
        }
        return false;
    }

    /**
     * Whether an Aura's SPELL effects make it enchant a card in a graveyard rather than a permanent
     * on the battlefield (a reanimation Aura such as Animate Dead). When true the Aura's cast-time
     * target is the graveyard card, so the default battlefield-permanent target must not be added.
     */
    private static boolean enchantsGraveyardCard(List<CardEffect> spellEffects) {
        return spellEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
    }

    private static void collectTargetTypes(CardEffect e, Set<TargetType> out) {
        TargetSpec spec = e.targetSpec();
        if (spec.admits(TargetPredicate.Kind.PLAYER)) out.add(TargetType.PLAYER);
        if (spec.admits(TargetPredicate.Kind.PERMANENT)) out.add(TargetType.PERMANENT);
        if (targetsSpellOnStack(e)) out.add(TargetType.SPELL_ON_STACK);
        if (spec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) out.add(TargetType.GRAVEYARD);
        if (spec.admits(TargetPredicate.Kind.EXILED_CARD)) out.add(TargetType.EXILE);
    }
}
