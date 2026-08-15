package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerationEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.StaticCreatureBoostEffect;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.KeywordGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shared target selection logic for AI spell casting.
 */
class AiTargetSelector {

    record SpellTargetSelection(UUID targetId, List<UUID> targetIds) {
        SpellTargetSelection {
            targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
        }
    }

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetValidationService targetValidationService;
    private final TargetLegalityService targetLegalityService;
    private final ValidTargetService validTargetService;
    private final AmountEvaluationService amountEvaluationService;
    private final TargetPolarityClassifier polarityClassifier;
    private final BoardEvaluator boardEvaluator;
    private final SizeGatedRemovalPump sizeGatedRemovalPump;

    AiTargetSelector(GameQueryService gameQueryService, TargetValidationService targetValidationService,
                     TargetLegalityService targetLegalityService) {
        this(gameQueryService, targetValidationService, targetLegalityService, null);
    }

    AiTargetSelector(GameQueryService gameQueryService, TargetValidationService targetValidationService,
                     TargetLegalityService targetLegalityService, BoardEvaluator boardEvaluator) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = new PredicateEvaluationService(gameQueryService);
        this.targetValidationService = targetValidationService;
        this.targetLegalityService = targetLegalityService;
        this.validTargetService = new ValidTargetService(gameQueryService, predicateEvaluationService,
                targetLegalityService, targetValidationService,
                new TargetPredicateEvaluationService(predicateEvaluationService, targetLegalityService));
        this.amountEvaluationService = new AmountEvaluationService(predicateEvaluationService, gameQueryService);
        this.polarityClassifier = new TargetPolarityClassifier(amountEvaluationService);
        this.boardEvaluator = boardEvaluator;
        this.sizeGatedRemovalPump = new SizeGatedRemovalPump(gameQueryService, amountEvaluationService);
    }

    UUID chooseTarget(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);

        // Handle player-only targeting (e.g. Haunting Echoes, Mind Rot)
        // Use base-mode targeting since AI never kicks spells
        Set<TargetType> allowedTargets = computeBaseAllowedTargets(card);
        if (allowedTargets.contains(TargetType.PLAYER) && !allowedTargets.contains(TargetType.PERMANENT)) {
            if (opponentId != null
                    && !gameQueryService.playerHasShroud(gameData, opponentId)
                    && !gameQueryService.playerHasHexproof(gameData, opponentId)) {
                return opponentId;
            }
            return null;
        }

        // Handle graveyard targeting (e.g. Unburial Rites, Gruesome Encore)
        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            List<Card> candidates = findValidGraveyardTargets(gameData, card, aiPlayerId);
            if (candidates.isEmpty()) return null;
            // Pick the highest mana value card (best reanimation/value target)
            return candidates.stream()
                    .max(Comparator.comparingInt(Card::getManaValue))
                    .map(Card::getId)
                    .orElse(null);
        }

        // Harmful shapes must not fall into the general fallback below — its
        // own-battlefield-first search would aim them at the AI's own permanents (e.g.
        // Quicksilver Geyser bouncing the AI's own artifact, Stun tapping the AI's own
        // blocker). TargetPolarityClassifier is the single source of truth for which
        // shapes are harmful; a guard test keeps it exhaustive over the card pool.
        // Removal and damage route here, before the aura branches (as they always have);
        // other harm routes after them so aura-specific handling keeps precedence.
        TargetPolarity polarity = polarityClassifier.classifyCard(gameData, card, aiPlayerId);
        if (polarity == TargetPolarity.HARMFUL_REMOVAL) {
            return chooseRemovalTarget(gameData, card, aiPlayerId, opponentId);
        }
        if (polarity == TargetPolarity.HARMFUL_DAMAGE) {
            return chooseHarmfulPermanentTarget(gameData, card, aiPlayerId, opponentId, allowedTargets);
        }

        // Controller-beneficial land auras (mana ramp like Wild Growth / Fertile Ground) help
        // whoever controls the enchanted land, so enchant one of the AI's own lands rather than
        // handing the opponent extra mana.
        if (isControllerBeneficialLandAura(card)) {
            return chooseOwnLandAuraTarget(gameData, card, aiPlayerId);
        }

        boolean isBeneficial = false;
        if (card.isAura()) {
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if ((effect instanceof StaticCreatureBoostEffect boost
                        && (boost.scope() == GrantScope.ENCHANTED_CREATURE || boost.scope() == GrantScope.EQUIPPED_CREATURE))
                        || (effect instanceof KeywordGrantingEffect grant && grant.scope() == GrantScope.ENCHANTED_CREATURE)) {
                    isBeneficial = true;
                    break;
                }
            }
        }

        if (isBeneficial) {
            // Target own creature with highest toughness
            List<Permanent> ownBattlefield = gameData.playerBattlefields.get(aiPlayerId);
            if (ownBattlefield != null) {
                UUID target = ownBattlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                        .max(Comparator.comparingInt(p -> gameQueryService.getEffectiveToughness(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
                if (target != null) return target;
            }
            return null; // Aura was handled by specific logic — don't fall through
        } else if (card.isAura()) {
            // Detrimental aura — target opponent's most threatening creature that doesn't already have this effect
            List<Permanent> oppBattlefield = gameData.playerBattlefields.get(opponentId);
            if (oppBattlefield != null) {
                List<Class<? extends CardEffect>> auraEffectClasses = card.getEffects(EffectSlot.STATIC).stream()
                        .map(CardEffect::getClass)
                        .toList();
                UUID target = oppBattlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                        .filter(p -> auraEffectClasses.stream().noneMatch(ec -> gameQueryService.hasAuraWithEffect(gameData, p, ec)))
                        .max(Comparator.comparingDouble(p -> threatScore(gameData, p, opponentId, aiPlayerId)))
                        .map(Permanent::getId)
                        .orElse(null);
                if (target != null) return target;
            }
            return null; // Aura was handled by specific logic — don't fall through
        }

        // Other harm (tap-downs, untap-step locks, "can't block", -1/-1 counters, debuffs,
        // forced attacks, control steal) aims at the opponent's board; see the polarity
        // routing comment above.
        if (polarity == TargetPolarity.HARMFUL) {
            return chooseHarmfulPermanentTarget(gameData, card, aiPlayerId, opponentId, allowedTargets);
        }

        // Beneficial non-aura spells (Fit of Rage, Giant Growth, …) — own board by default.
        // Exception: pump an undersized opponent creature so a size-gated removal
        // (Smite the Monstrous, etc.) becomes legal — that kill line beats a self-pump.
        if (polarity == TargetPolarity.BENEFICIAL) {
            List<Permanent> enableKill = sizeGatedRemovalPump.findEnabledOpponentCreatures(
                    gameData, card, aiPlayerId, opponentId);
            UUID killSetupTarget = enableKill.stream()
                    .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                    .max(Comparator.comparingDouble(p -> threatScore(gameData, p, opponentId, aiPlayerId)))
                    .map(Permanent::getId)
                    .orElse(null);
            if (killSetupTarget != null) {
                return killSetupTarget;
            }
            return gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of()).stream()
                    .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                    .max(Comparator.comparingInt(p -> {
                        if (!gameQueryService.isCreature(gameData, p)) {
                            return 0;
                        }
                        return gameQueryService.getEffectivePower(gameData, p)
                                + gameQueryService.getEffectiveToughness(gameData, p);
                    }))
                    .map(Permanent::getId)
                    .orElse(null);
        }

        // General fallback: find any valid target using target filter + effect validators
        // Search own battlefield first (for beneficial ETB effects like Awakener Druid)
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        for (Permanent p : ownBattlefield) {
            if (isValidPermanentTarget(gameData, card, p, aiPlayerId)) {
                return p.getId();
            }
        }
        // Then search opponent battlefield — prefer the most threatening valid target so that
        // any remaining unclassified disruption shape still attacks the real threat
        // (e.g. a 2/2 lord pumping four other creatures) rather than whichever creature happens
        // to come first in the battlefield list.
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        UUID oppTarget = oppBattlefield.stream()
                .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                .max(Comparator.comparingDouble(p -> generalTargetPriority(gameData, p, opponentId, aiPlayerId)))
                .map(Permanent::getId)
                .orElse(null);
        if (oppTarget != null) {
            return oppTarget;
        }

        // No valid permanent targets — fall back to targeting the opponent if the spell allows it
        if (allowedTargets.contains(TargetType.PLAYER)) {
            return opponentId;
        }

        return null;
    }

    /**
     * True for an aura whose only benefit accrues to the controller of the enchanted land — a
     * mana-ramp land aura like Wild Growth, Fertile Ground, or Overgrowth (an
     * {@link AddManaOnEnchantedLandTapEffect} in the land-tap slot). Such auras must be attached
     * to one of the AI's own lands; enchanting the opponent's land would ramp the opponent.
     */
    private static boolean isControllerBeneficialLandAura(Card card) {
        return card.isAura() && card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND).stream()
                .anyMatch(AddManaOnEnchantedLandTapEffect.class::isInstance);
    }

    /**
     * Picks the AI's own best land for a controller-beneficial land aura, preferring an untapped
     * land so the extra mana is usable this turn. Returns null if the AI controls no legal land.
     */
    private UUID chooseOwnLandAuraTarget(GameData gameData, Card card, UUID aiPlayerId) {
        return gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of()).stream()
                .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                .max(Comparator.comparing((Permanent p) -> !p.isTapped()))
                .map(Permanent::getId)
                .orElse(null);
    }

    /**
     * Selects the best spell on the stack to counter. Examines all opponent spells on the stack
     * and picks the one with the highest mana value (most impactful). Respects the counterspell's
     * target filter (e.g. Essence Scatter only targets creature spells, Negate only non-creature).
     * Skips uncounterable spells and the AI's own spells.
     *
     * @return the card ID of the targeted spell, or null if no valid target exists
     */
    UUID chooseSpellTarget(GameData gameData, Card counterSpell, UUID aiPlayerId) {
        return chooseSpellTarget(gameData, counterSpell.getTargetFilter(), aiPlayerId);
    }

    /**
     * Overload for activated abilities that target spells (e.g. Spiketail Hatchling).
     * Accepts a TargetFilter directly instead of extracting it from a Card.
     */
    UUID chooseSpellTarget(GameData gameData, TargetFilter targetFilter, UUID aiPlayerId) {
        StackEntry bestTarget = null;
        double bestValue = 0;

        for (StackEntry entry : gameData.stack) {
            // Skip abilities — counterspells only target spells
            if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                    || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
                continue;
            }
            // Don't counter our own spells
            if (entry.getControllerId().equals(aiPlayerId)) {
                continue;
            }
            // Skip uncounterable spells
            if (gameQueryService.isUncounterable(gameData, entry.getCard())) {
                continue;
            }
            // Validate against the counterspell's target filter (e.g. creature-only, non-creature-only)
            if (targetLegalityService.checkSpellTargetOnStack(gameData, entry.getCard().getId(),
                    targetFilter, aiPlayerId).isPresent()) {
                continue;
            }
            // Evaluate how valuable this spell is — higher mana value = bigger threat
            double value = evaluateSpellOnStack(entry);
            if (value > bestValue) {
                bestValue = value;
                bestTarget = entry;
            }
        }

        return bestTarget != null ? bestTarget.getCard().getId() : null;
    }

    /**
     * Evaluates how valuable/threatening a spell on the stack is. Used to decide which
     * spell is most worth countering. Creatures are scored by their combat stats plus
     * mana value, non-creature spells by mana value as a proxy for impact.
     */
    private double evaluateSpellOnStack(StackEntry entry) {
        Card card = entry.getCard();
        double manaValueScore = card.getManaValue() * 3.0;

        if (entry.getEntryType() == StackEntryType.CREATURE_SPELL) {
            int power = card.getPower() != null ? card.getPower() : 0;
            int toughness = card.getToughness() != null ? card.getToughness() : 0;
            return manaValueScore + power * 3.0 + toughness * 1.5;
        }

        return manaValueScore;
    }

    /**
     * True when the spell must go through {@link #chooseMultiTargets} instead of the
     * single-target {@link #chooseTarget}: either it declares several target groups, or its
     * one group accepts more than one target ("up to N" spells like Feeling of Dread, which
     * previously took the single-target path and always submitted just one target).
     * X-scaled targeting (target count decided with X elsewhere), divided-damage spells
     * (damage-assignment path), and stack-targeting spells keep their existing paths.
     *
     * <p>So does a spell that charges mana per extra target (Fireball's {@code
     * additionalCostPerExtraTarget}): the AI's affordability check prices a cast by its mana cost
     * and X alone, so every target past the first would be mana it never taps and the engine would
     * reject the cast. Those spells take the single-target line — one target, no extra cost, and
     * for an evenly divided burn spell the hardest hit available.
     */
    boolean needsMultiTargetSelection(Card card) {
        List<SpellTarget> groups = card.getSpellTargets();
        if (groups.size() > 1) {
            return true;
        }
        return groups.size() == 1
                && card.getMaxTargets() > 1
                && card.getAdditionalCostPerExtraTarget() <= 0
                && !card.hasXScaledTargets()
                && !EffectResolution.needsDamageDistribution(card)
                && !EffectResolution.needsSpellTarget(card);
    }

    /**
     * Returns whether a spell has a cast-time graveyard target carried separately from its
     * ordinary spell-target groups.
     */
    boolean hasSeparateGraveyardTarget(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
    }

    /**
     * Selects both target channels for spells that combine a graveyard target with ordinary
     * target groups. The engine stores the graveyard card in {@code targetId} and the ordinary
     * targets in {@code targetIds}; combining them into one list would make the announcement
     * invalid even when every individual choice is legal.
     */
    SpellTargetSelection chooseSeparateGraveyardTargets(GameData gameData, Card card, UUID aiPlayerId) {
        List<Card> graveyardCandidates = findValidGraveyardTargets(gameData, card, aiPlayerId);
        UUID graveyardTarget = graveyardCandidates.stream()
                .max(Comparator.comparingInt(Card::getManaValue))
                .map(Card::getId)
                .orElse(null);

        boolean graveyardTargetRequired = card.getEffects(EffectSlot.SPELL).stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                .map(this::unwrapConditionalEffect)
                .anyMatch(effect -> !(effect instanceof ReturnCardFromGraveyardEffect returnEffect)
                        || !returnEffect.upTo());
        if (graveyardTarget == null && graveyardTargetRequired) {
            return null;
        }

        List<UUID> ordinaryTargets = chooseMultiTargets(gameData, card, aiPlayerId);
        if (ordinaryTargets == null) {
            return null;
        }
        return new SpellTargetSelection(graveyardTarget, ordinaryTargets);
    }

    private CardEffect unwrapConditionalEffect(CardEffect effect) {
        return effect instanceof ConditionalEffect conditional ? conditional.wrapped() : effect;
    }

    /**
     * Selects targets for multi-target spells (several target groups, or one group that
     * accepts several targets). Returns the flat target list in group order, or null if
     * a group's mandatory targets cannot be satisfied.
     */
    List<UUID> chooseMultiTargets(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<SpellTarget> spellTargets = card.getSpellTargets();
        List<UUID> result = new ArrayList<>();
        Set<UUID> alreadyChosen = new HashSet<>();

        for (SpellTarget st : spellTargets) {
            int effectiveMaxTargets = targetLegalityService.getEffectiveMaxTargetsForGroup(
                    gameData, card, aiPlayerId, null, st);
            List<CardEffect> groupEffects = findEffectsForTargetGroup(card, st.getIndex());

            boolean wantsPlayer = groupEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                    || targetFilterAllowsPlayer(st.getFilter());
            boolean wantsPermanent = groupEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    || targetFilterAllowsPermanent(st.getFilter());

            if (wantsPlayer && !wantsPermanent) {
                if (effectiveMaxTargets == 0) {
                    if (st.getMinTargets() > 0) {
                        return null;
                    }
                    continue;
                }
                UUID chosen = pickPlayerTargetForGroup(
                        gameData, aiPlayerId, opponentId, st.getFilter(), groupEffects);
                if (chosen != null) {
                    result.add(chosen);
                    alreadyChosen.add(chosen);
                } else if (st.getMinTargets() > 0) {
                    return null; // Mandatory target cannot be satisfied
                }
            } else if (wantsPermanent) {
                List<UUID> chosen = pickPermanentTargetsForGroup(gameData, card, aiPlayerId, opponentId,
                        st, effectiveMaxTargets, alreadyChosen, groupEffects);
                if (chosen.size() < st.getMinTargets() && wantsPlayer) {
                    UUID player = pickPlayerTargetForGroup(
                            gameData, aiPlayerId, opponentId, st.getFilter(), groupEffects);
                    if (player != null && !alreadyChosen.contains(player)
                            && !chosen.contains(player) && chosen.size() < effectiveMaxTargets) {
                        chosen.add(player);
                    }
                }
                if (chosen.size() < st.getMinTargets()) {
                    return null; // Mandatory targets cannot be satisfied
                }
                result.addAll(chosen);
                alreadyChosen.addAll(chosen);
            } else if (st.getMinTargets() > 0) {
                return null; // Mandatory target cannot be satisfied
            }
        }

        return result;
    }

    private static boolean targetFilterAllowsPlayer(TargetFilter targetFilter) {
        return targetFilter instanceof AnyTargetPredicateTargetFilter
                || targetFilter instanceof PlayerPredicateTargetFilter;
    }

    private static boolean targetFilterAllowsPermanent(TargetFilter targetFilter) {
        return targetFilter instanceof AnyTargetPredicateTargetFilter
                || targetFilter instanceof ControlledPermanentPredicateTargetFilter
                || targetFilter instanceof OwnedPermanentPredicateTargetFilter
                || targetFilter instanceof PermanentPredicateTargetFilter;
    }

    /**
     * Returns all effects on the card that are mapped to the given target group index.
     */
    private List<CardEffect> findEffectsForTargetGroup(Card card, int targetIndex) {
        List<CardEffect> result = new ArrayList<>();
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (card.getEffectTargetIndex(effect) == targetIndex) {
                result.add(effect);
            }
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (card.getEffectTargetIndex(effect) == targetIndex) {
                result.add(effect);
            }
        }
        return result;
    }

    /**
     * Picks a player target for a multi-target group. Targets self for beneficial effects
     * (e.g. ExtraTurnEffect), opponent for harmful effects.
     */
    private UUID pickPlayerTargetForGroup(GameData gameData, UUID aiPlayerId, UUID opponentId,
                                          TargetFilter groupFilter, List<CardEffect> effects) {
        boolean isBeneficial = effects.stream().anyMatch(ExtraTurnEffect.class::isInstance);

        UUID preferred = isBeneficial ? aiPlayerId : opponentId;
        UUID fallback = isBeneficial ? opponentId : aiPlayerId;

        if (isLegalPlayerTargetForGroup(gameData, aiPlayerId, preferred, groupFilter)
                && (isBeneficial || !gameQueryService.playerHasHexproof(gameData, preferred))) {
            return preferred;
        }
        if (isLegalPlayerTargetForGroup(gameData, aiPlayerId, fallback, groupFilter)
                && (!isBeneficial || !gameQueryService.playerHasHexproof(gameData, fallback))) {
            return fallback;
        }
        return null;
    }

    private boolean isLegalPlayerTargetForGroup(GameData gameData, UUID aiPlayerId, UUID playerId,
                                                TargetFilter groupFilter) {
        if (playerId == null || gameQueryService.isPeaceTalksActive(gameData)
                || gameQueryService.playerHasShroud(gameData, playerId)) {
            return false;
        }
        if (groupFilter instanceof PlayerPredicateTargetFilter playerFilter) {
            return targetLegalityService.matchesPlayerPredicate(
                    gameData, aiPlayerId, playerId, playerFilter.predicate());
        }
        if (groupFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
            return targetLegalityService.matchesPlayerPredicate(
                    gameData, aiPlayerId, playerId, anyFilter.playerPredicate());
        }
        return groupFilter == null;
    }

    /**
     * Picks up to {@code maxTargets} permanents for one multi-target group, using the
     * group's filter. The group's {@link TargetPolarity} decides which board to draw from:
     * harmful (and unclassified) groups take the opponent's most threatening valid
     * permanents, beneficial groups the AI's own best. Optional targets beyond the group's
     * minimum are never padded from the other board — an "up to N" tap spell taps as many
     * opponent creatures as it legally can and stops, instead of filling the quota with the
     * AI's own permanents. Only an unmet minimum forces picks from the other board (best
     * candidate first, e.g. Pounce's own-fighter group choosing the AI's strongest creature).
     */
    private List<UUID> pickPermanentTargetsForGroup(GameData gameData, Card card, UUID aiPlayerId,
                                                    UUID opponentId, SpellTarget st, int maxTargets,
                                                    Set<UUID> alreadyChosen, List<CardEffect> groupEffects) {
        TargetFilter groupFilter = st.getFilter();
        boolean beneficial = polarityClassifier.classifyGroup(gameData, groupEffects, aiPlayerId)
                == TargetPolarity.BENEFICIAL;
        UUID preferredBoard = beneficial ? aiPlayerId : opponentId;
        UUID fallbackBoard = beneficial ? opponentId : aiPlayerId;

        List<UUID> chosen = new ArrayList<>();
        takeGroupTargets(gameData, card, aiPlayerId, opponentId, groupFilter, preferredBoard,
                maxTargets, alreadyChosen, chosen);
        if (chosen.size() < st.getMinTargets()) {
            takeGroupTargets(gameData, card, aiPlayerId, opponentId, groupFilter, fallbackBoard,
                    Math.min(st.getMinTargets(), maxTargets), alreadyChosen, chosen);
        }
        return chosen;
    }

    /**
     * Appends the given board's valid group targets to {@code chosen}, best priority first,
     * until it holds {@code limit} targets or the board runs out of legal candidates.
     */
    private void takeGroupTargets(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId,
                                  TargetFilter groupFilter, UUID boardOwner, int limit,
                                  Set<UUID> alreadyChosen, List<UUID> chosen) {
        if (boardOwner == null || chosen.size() >= limit) {
            return;
        }
        UUID boardOpponent = boardOwner.equals(aiPlayerId) ? opponentId : aiPlayerId;
        List<Permanent> candidates = gameData.playerBattlefields.getOrDefault(boardOwner, List.of()).stream()
                .filter(p -> !alreadyChosen.contains(p.getId()) && !chosen.contains(p.getId()))
                .filter(p -> validTargetService.isValidMultiTargetPermanent(gameData, card, p, aiPlayerId, groupFilter))
                .sorted(Comparator.comparingDouble(
                        (Permanent p) -> generalTargetPriority(gameData, p, boardOwner, boardOpponent)).reversed())
                .toList();
        for (Permanent candidate : candidates) {
            if (chosen.size() >= limit) {
                return;
            }
            // Checked against picks this call already made as well as earlier groups' — the
            // restriction covers the whole chosen set, and a stream filter would only ever see
            // the state the pipeline started with.
            if (!satisfiesMultiTargetConstraint(gameData, card, candidate, alreadyChosen, chosen)) {
                continue;
            }
            chosen.add(candidate.getId());
        }
    }

    /**
     * Whether adding {@code candidate} to the targets chosen so far still satisfies the card's
     * cross-target restriction (CR 601.2c), which the per-position filters can't express. The
     * engine rejects an announcement that violates it, so a candidate failing this check must be
     * passed over rather than submitted.
     */
    private boolean satisfiesMultiTargetConstraint(GameData gameData, Card card, Permanent candidate,
                                                   Set<UUID> alreadyChosen, List<UUID> chosen) {
        MultiTargetConstraint constraint = card.getMultiTargetConstraint();
        if (constraint == null) {
            return true;
        }
        Set<UUID> chosenSoFar = new HashSet<>(alreadyChosen);
        chosenSoFar.addAll(chosen);
        if (constraint == MultiTargetConstraint.AT_MOST_TWO_CREATURES_AND_TWO_LANDS) {
            List<UUID> trial = new ArrayList<>(chosenSoFar);
            trial.add(candidate.getId());
            return targetLegalityService.fitsAtMostTwoCreaturesAndTwoLands(gameData, trial);
        }
        if (constraint == MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER
                || constraint == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE) {
            UUID candidateControllerId = gameQueryService.findPermanentController(gameData, candidate.getId());
            return chosenSoFar.stream()
                    .map(id -> gameQueryService.findPermanentController(gameData, id))
                    .noneMatch(candidateControllerId::equals);
        }
        for (UUID chosenId : chosenSoFar) {
            Permanent other = gameQueryService.findPermanentById(gameData, chosenId);
            UUID candidateControllerId = gameQueryService.findPermanentController(gameData, candidate.getId());
            if (other == null) {
                // A chosen player target imposes no permanent-to-permanent restriction, except for
                // "controlled by the first target", where that player is the required controller.
                if (constraint == MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET
                        && !chosenId.equals(candidateControllerId)) {
                    return false;
                }
                continue;
            }
            boolean compatible = switch (constraint) {
                case SHARE_NO_CREATURE_TYPES -> !gameQueryService.shareCreatureType(gameData, other, candidate);
                case SHARE_ARTIFACT_CREATURE_OR_LAND_TYPE ->
                        gameQueryService.sharesArtifactCreatureOrLandType(other, candidate);
                case SHARE_ARTIFACT_OR_CREATURE_TYPE ->
                        gameQueryService.sharesArtifactOrCreatureType(other, candidate);
                case CONTROLLED_BY_FIRST_TARGET -> java.util.Objects.equals(candidateControllerId,
                        gameQueryService.findPermanentController(gameData, other.getId()));
                case ATTACHED_TO_FIRST_TARGET -> java.util.Objects.equals(other.getId(), candidate.getAttachedTo());
                case AT_MOST_TWO_CREATURES_AND_TWO_LANDS, AT_MOST_ONE_PER_CONTROLLER,
                     ONE_PER_CONTROLLER_IF_ABLE, AT_MOST_ONE_PER_COLOR -> true; // handled above
                case SAME_CREATURE_OR_LAND_TYPE_AS_FIRST_AURA_HOST ->
                        isAnotherPermanentOfAuraHostType(gameData, other, candidate);
            };
            if (!compatible) {
                return false;
            }
        }
        return true;
    }

    private boolean isAnotherPermanentOfAuraHostType(GameData gameData, Permanent aura, Permanent candidate) {
        if (aura == null || candidate == null || !aura.isAttached()) {
            return false;
        }
        Permanent host = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (host == null || host.getId().equals(candidate.getId())) {
            return false;
        }
        return (gameQueryService.isCreature(gameData, host) && gameQueryService.isCreature(gameData, candidate))
                || (gameQueryService.isLand(gameData, host) && gameQueryService.isLand(gameData, candidate));
    }

    boolean isValidPermanentTarget(GameData gameData, Card card, Permanent target, UUID aiPlayerId) {
        // Use the same full validation as the frontend UI (protection, hexproof, shroud,
        // target filter, and "any target" creature/planeswalker restriction)
        if (!validTargetService.isValidSpellPermanentTarget(gameData, card, target, aiPlayerId)) {
            return false;
        }
        // Run the same @ValidatesTarget validators that spell casting uses
        TargetValidationContext ctx = new TargetValidationContext(gameData, target.getId(), null, card);
        if (targetValidationService.checkEffectTargets(card.getEffects(EffectSlot.SPELL), ctx).isPresent()) {
            return false;
        }
        if (targetValidationService.checkEffectTargets(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD), ctx).isPresent()) {
            return false;
        }
        return true;
    }

    /**
     * Player-side counterpart of {@link #isValidPermanentTarget}: true when {@code card} could be
     * cast targeting {@code playerTargetId}, judged by the very check the cast path runs
     * ({@code TargetLegalityService.validateSpellTargeting}) — hexproof/shroud, protection, player
     * predicates and the {@code @ValidatesTarget} validators.
     *
     * <p>An allowed-target set that merely <em>includes</em> players is not enough to offer one: a
     * live multi-target scope declares the no-op {@code PLAYER_OR_PERMANENT} spec (Synchronized
     * Strike's untap), so its permanent-only spell looks player-targetable until this check runs.
     */
    boolean isValidPlayerTarget(GameData gameData, Card card, UUID playerTargetId, UUID aiPlayerId) {
        return targetLegalityService.checkSpellTargeting(gameData, card, playerTargetId, null, aiPlayerId).isEmpty();
    }

    /**
     * Computes allowed target types using only the base (un-kicked) mode of effects.
     * AI never kicks spells, so this prevents including target types that are only
     * valid for the kicked mode (e.g. Fight with Fire's kicked mode can target players,
     * but the base mode only targets creatures).
     */
    Set<TargetType> computeBaseAllowedTargets(Card card) {
        Set<TargetType> result = EnumSet.noneOf(TargetType.class);
        if (card.isAura()) {
            if (card.isEnchantPlayer()) {
                result.add(TargetType.PLAYER);
            } else {
                result.add(TargetType.PERMANENT);
            }
        }
        for (CardEffect e : card.getEffects(EffectSlot.SPELL)) {
            CardEffect effectToCheck = e;
            if (e instanceof ConditionalReplacementEffect replacement) {
                effectToCheck = replacement.baseEffect();
            }
            TargetSpec spec = effectToCheck.targetSpec();
            if (spec.admits(TargetPredicate.Kind.PLAYER)) result.add(TargetType.PLAYER);
            if (spec.admits(TargetPredicate.Kind.PERMANENT)) result.add(TargetType.PERMANENT);
            if (EffectResolution.targetsSpellOnStack(effectToCheck)) result.add(TargetType.SPELL_ON_STACK);
            if (spec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) result.add(TargetType.GRAVEYARD);
            if (spec.admits(TargetPredicate.Kind.EXILED_CARD)) result.add(TargetType.EXILE);
        }
        for (CardEffect e : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            TargetSpec spec = e.targetSpec();
            if (spec.admits(TargetPredicate.Kind.PLAYER)) result.add(TargetType.PLAYER);
            if (spec.admits(TargetPredicate.Kind.PERMANENT)) result.add(TargetType.PERMANENT);
        }
        return result;
    }

    /**
     * Picks a target for a spell or ETB whose effect is harmful to the targeted permanent
     * (damage, tap-down, debuff, forced attack, steal). Aims at the opponent's most
     * threatening legal permanent, then (for any-target damage) the opponent's face. Only a
     * mandatory target with no legal opponent choice (e.g. a damage ETB while the opponent's
     * board is empty) falls onto the AI's own board, giving up the least valuable permanent.
     */
    private UUID chooseHarmfulPermanentTarget(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId,
                                              Set<TargetType> allowedTargets) {
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        UUID oppTarget = oppBattlefield.stream()
                .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                .max(Comparator.comparingDouble(p -> generalTargetPriority(gameData, p, opponentId, aiPlayerId)))
                .map(Permanent::getId)
                .orElse(null);
        if (oppTarget != null) {
            return oppTarget;
        }
        if (allowedTargets.contains(TargetType.PLAYER)
                && opponentId != null
                && !gameQueryService.playerHasShroud(gameData, opponentId)
                && !gameQueryService.playerHasHexproof(gameData, opponentId)) {
            return opponentId;
        }
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        return findRemovalCandidate(gameData, card, ownBattlefield, aiPlayerId, true);
    }

    private UUID chooseRemovalTarget(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId) {
        // Search opponent's battlefield first
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        UUID oppTarget = findRemovalCandidate(gameData, card, oppBattlefield, aiPlayerId, false);
        if (oppTarget != null) {
            return oppTarget;
        }

        // No legal opponent target — a mandatory target forces the removal onto the AI's
        // own board, so pick the least valuable legal permanent, not the best one.
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        return findRemovalCandidate(gameData, card, ownBattlefield, aiPlayerId, true);
    }

    /**
     * Returns all valid permanent targets for an X spell whose target filter includes a mana-value
     * constraint, filtered to those reachable with an announced X up to maxAffordableX.
     */
    List<Permanent> findValidPermanentTargetsForManaValueX(GameData gameData, Card card,
                                                            UUID aiPlayerId, int maxAffordableX) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<Permanent> result = new ArrayList<>();
        boolean allowsZeroManaValue = AiUtils.hasManaValueAtMostXTarget(card);
        // Search opponent's battlefield first (more likely target for steal effects)
        for (UUID playerId : new UUID[]{opponentId, aiPlayerId}) {
            if (playerId == null) continue;
            for (Permanent p : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                int mv = p.getCard().getManaValue();
                if ((allowsZeroManaValue || mv >= 1)
                        && mv <= maxAffordableX
                        && isValidPermanentTarget(gameData, card, p, aiPlayerId)) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    /**
     * Returns all valid graveyard cards that the given spell can target.
     * Examines the card's SPELL effects to determine the correct graveyard scope and filter.
     */
    List<Card> findValidGraveyardTargets(GameData gameData, Card card, UUID aiPlayerId) {
        return findValidGraveyardTargets(gameData, card, aiPlayerId, Integer.MAX_VALUE);
    }

    /**
     * Returns all valid graveyard cards that the given spell can target,
     * filtering by mana value for requiresManaValueEqualsX effects.
     *
     * @param maxAffordableX the maximum affordable X value — candidates with mana value
     *                       exceeding this are excluded for requiresManaValueEqualsX effects
     */
    List<Card> findValidGraveyardTargets(GameData gameData, Card card, UUID aiPlayerId, int maxAffordableX) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) continue;

            List<Card> candidates;
            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                candidates = getGraveyardCandidates(gameData, rge.source(), aiPlayerId, opponentId);
                if (rge.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(c, rge.filter(), card.getId()))
                            .toList();
                }
                if (rge.requiresManaValueEqualsX() && maxAffordableX < Integer.MAX_VALUE) {
                    candidates = candidates.stream()
                            .filter(c -> c.getManaValue() >= 1 && c.getManaValue() <= maxAffordableX)
                            .toList();
                }
                if (rge.requiresManaValueAtMostX() && maxAffordableX < Integer.MAX_VALUE) {
                    candidates = candidates.stream()
                            .filter(c -> c.getManaValue() <= maxAffordableX)
                            .toList();
                }
            } else {
                GraveyardSearchScope scope = effect.targetSpec().graveyardScope().orElseThrow();
                candidates = getGraveyardCandidates(gameData, scope, aiPlayerId, opponentId);

                // Apply card-type filters matching what GraveyardTargetValidators enforces
                if (effect instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect) {
                    candidates = candidates.stream().filter(c -> c.hasType(CardType.CREATURE)).toList();
                } else if (effect instanceof CastTargetInstantOrSorceryFromGraveyardEffect) {
                    candidates = candidates.stream()
                            .filter(c -> c.hasType(CardType.INSTANT) || c.hasType(CardType.SORCERY)).toList();
                } else if (effect instanceof ExileGraveyardCardsEffect e
                        && e.scope() == GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD && e.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(c, e.filter(), card.getId())).toList();
                } else if (effect instanceof GrantFlashbackToTargetGraveyardCardEffect e) {
                    candidates = candidates.stream()
                            .filter(c -> e.cardTypes().stream().anyMatch(c::hasType)).toList();
                } else if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect e && e.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(c, e.filter(), card.getId())).toList();
                } else if (effect instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect) {
                    candidates = candidates.stream()
                            .filter(c -> c.hasType(CardType.ARTIFACT) || c.hasType(CardType.CREATURE)).toList();
                } else if (effect instanceof ExileTargetGraveyardCardAndSameNameFromZonesEffect) {
                    candidates = candidates.stream()
                            .filter(c -> !(c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))).toList();
                }
            }

            CardEffect effectToValidate = effect instanceof ConditionalEffect conditional
                    ? conditional.wrapped()
                    : effect;
            candidates = candidates.stream()
                    .filter(candidate -> targetValidationService.checkEffectTargets(
                            List.of(effectToValidate),
                            new TargetValidationContext(gameData, candidate.getId(), Zone.GRAVEYARD,
                                    card, candidate.getManaValue())).isEmpty())
                    .toList();

            if (!candidates.isEmpty()) {
                return new ArrayList<>(candidates);
            }
        }
        return List.of();
    }

    /**
     * Returns the controller's graveyard cards that can participate in a
     * {@link ReturnTargetCardsFromGraveyardToHandEffect} choice. These effects use the engine's
     * special cast-time graveyard choice flow and therefore do not expose an ordinary graveyard
     * {@code TargetSpec}.
     */
    List<Card> findValidGraveyardReturnTargets(GameData gameData, Card card, UUID aiPlayerId,
                                               ReturnTargetCardsFromGraveyardToHandEffect effect) {
        List<Card> candidates = gameData.playerGraveyards.getOrDefault(aiPlayerId, List.of()).stream()
                .filter(candidate -> predicateEvaluationService.matchesCardPredicate(
                        candidate, effect.filter(), card.getId()))
                .toList();
        if (!effect.requireSharedCreatureType()) {
            return candidates;
        }
        return candidates.stream()
                .filter(candidate -> candidates.stream().anyMatch(other ->
                        !candidate.getId().equals(other.getId())
                                && gameQueryService.shareCreatureType(candidate, other)))
                .toList();
    }

    private List<Card> getGraveyardCandidates(GameData gameData, GraveyardSearchScope scope,
                                               UUID aiPlayerId, UUID opponentId) {
        List<Card> candidates = new ArrayList<>();
        switch (scope) {
            case CONTROLLERS_GRAVEYARD -> candidates.addAll(
                    gameData.playerGraveyards.getOrDefault(aiPlayerId, List.of()));
            case OPPONENT_GRAVEYARD -> candidates.addAll(
                    gameData.playerGraveyards.getOrDefault(opponentId, List.of()));
            case ALL_GRAVEYARDS -> {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    candidates.addAll(gameData.playerGraveyards.getOrDefault(playerId, List.of()));
                }
            }
        }
        return candidates;
    }

    /**
     * Builds the announced division for "distribute X counters among any number of target creatures"
     * (Spoils of War), which rides on the same assignment map as divided damage. All X counters go on
     * a single creature — the AI's biggest for a benign counter, the opponent's biggest for -1/-1.
     * Returns {@code null} when the card has no such effect, or when X is 0 or no creature is legal
     * (in which case the AI simply declines to cast).
     */
    private Map<UUID, Integer> buildCounterDistribution(GameData gameData, Card card, UUID aiPlayerId) {
        DistributeCountersAmongTargetsEffect effect = card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e instanceof DistributeCountersAmongTargetsEffect d && d.mode() == DivisionMode.CHOSEN)
                .map(DistributeCountersAmongTargetsEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return null;
        }

        int total = amountEvaluationService.evaluate(gameData, effect.total(), AmountContext.forCasting(aiPlayerId));
        if (total <= 0) {
            return null;
        }

        UUID ownerId = effect.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                ? AiUtils.getOpponentId(gameData, aiPlayerId)
                : aiPlayerId;
        Permanent best = null;
        for (Permanent p : gameData.playerBattlefields.getOrDefault(ownerId, List.of())) {
            if (!gameQueryService.isCreature(gameData, p) || !isValidPermanentTarget(gameData, card, p, aiPlayerId)) {
                continue;
            }
            if (best == null || gameQueryService.getEffectivePower(gameData, p)
                    > gameQueryService.getEffectivePower(gameData, best)) {
                best = p;
            }
        }
        return best == null ? null : Map.of(best.getId(), total);
    }

    /**
     * Builds a damage assignment map for divided damage spells (e.g. Ignite Disorder, Fight with Fire kicked).
     * Distributes damage to maximize creature kills on the opponent's battlefield.
     * For "any targets" effects, dumps remaining damage on the opponent player.
     * Returns null if no valid targets exist.
     */
    Map<UUID, Integer> buildDamageAssignments(GameData gameData, Card card, UUID aiPlayerId) {
        Map<UUID, Integer> counterAssignments = buildCounterDistribution(gameData, card, aiPlayerId);
        if (counterAssignments != null) {
            return counterAssignments;
        }

        // Ignite Disorder: fixed total divided among target creatures (no players).
        DealDividedDamageEffect creaturesEffect = card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e instanceof DealDividedDamageEffect d
                        && d.mode() == DivisionMode.CHOSEN && !d.etbAssignments()
                        && !d.canTargetPlayers() && d.totalDamage() instanceof Fixed)
                .map(DealDividedDamageEffect.class::cast)
                .findFirst()
                .orElse(null);

        DealDividedDamageEffect anyTargetEffect = findDividedDamageAnyTargetsEffect(card);

        if (creaturesEffect == null && anyTargetEffect == null) {
            // X-damage divided among attacking creatures — only relevant during combat
            return null;
        }

        int totalDamage;
        boolean canTargetPlayers;
        int maxTargets;
        if (creaturesEffect != null) {
            totalDamage = ((Fixed) creaturesEffect.totalDamage()).value();
            canTargetPlayers = false;
            maxTargets = Math.max(1, card.getMaxTargets());
        } else {
            totalDamage = ((Fixed) anyTargetEffect.totalDamage()).value();
            canTargetPlayers = true;
            // "any number of targets" — no creature target limit
            maxTargets = Integer.MAX_VALUE;
        }
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);

        // Find valid creature targets on opponent's battlefield
        List<Permanent> validTargets = new ArrayList<>();
        for (Permanent p : gameData.playerBattlefields.getOrDefault(opponentId, List.of())) {
            if (gameQueryService.isCreature(gameData, p) && isValidPermanentTarget(gameData, card, p, aiPlayerId)) {
                validTargets.add(p);
            }
        }

        if (validTargets.isEmpty() && !canTargetPlayers) {
            return null;
        }

        if (validTargets.isEmpty() && canTargetPlayers) {
            // No creatures to kill — send all damage to the opponent
            return Map.of(opponentId, totalDamage);
        }

        // Sort by lethal damage needed (ascending) to maximize kills
        validTargets.sort(Comparator.comparingInt(p ->
                gameQueryService.getEffectiveToughness(gameData, p) - p.getMarkedDamage()));

        if (validTargets.size() > maxTargets) {
            validTargets = new ArrayList<>(validTargets.subList(0, maxTargets));
        }

        // Distribute damage greedily: assign lethal damage to weakest targets first
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        int remaining = totalDamage;

        for (Permanent target : validTargets) {
            if (remaining <= 0) break;
            int lethal = gameQueryService.getEffectiveToughness(gameData, target) - target.getMarkedDamage();
            int dmg = Math.min(remaining, Math.max(1, lethal));
            assignments.put(target.getId(), dmg);
            remaining -= dmg;
        }

        // Dump remaining damage on the opponent (if allowed) or the last assigned target
        if (remaining > 0) {
            if (canTargetPlayers) {
                assignments.put(opponentId, remaining);
            } else if (!assignments.isEmpty()) {
                UUID lastKey = null;
                for (UUID key : assignments.keySet()) {
                    lastKey = key;
                }
                assignments.merge(lastKey, remaining, Integer::sum);
            }
        }

        return assignments;
    }

    /**
     * Searches for a CHOSEN "any targets" DealDividedDamageEffect in the card's spell effects,
     * including inside kicker replacement wrappers.
     */
    private DealDividedDamageEffect findDividedDamageAnyTargetsEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (isChosenAnyTargets(effect)) {
                return (DealDividedDamageEffect) effect;
            }
            if (effect instanceof ConditionalReplacementEffect replacement
                    && isChosenAnyTargets(replacement.upgradedEffect())) {
                return (DealDividedDamageEffect) replacement.upgradedEffect();
            }
        }
        return null;
    }

    private boolean isChosenAnyTargets(CardEffect effect) {
        return effect instanceof DealDividedDamageEffect d
                && d.mode() == DivisionMode.CHOSEN && !d.etbAssignments()
                && d.canTargetPlayers() && d.totalDamage() instanceof Fixed;
    }

    private UUID findRemovalCandidate(GameData gameData, Card card, List<Permanent> battlefield,
                                      UUID aiPlayerId, boolean pickLeastValuable) {
        List<Permanent> candidates = battlefield.stream()
                .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, candidates.getFirst().getId());
        UUID opponentOfController = controllerId != null
                ? AiUtils.getOpponentId(gameData, controllerId)
                : null;

        if (pickLeastValuable) {
            // Forced self-removal: give up the least valuable legal candidate (weakest
            // creature, or cheapest non-creature) instead of the most threatening one.
            return candidates.stream()
                    .min(Comparator.comparingDouble(p -> generalTargetPriority(gameData, p, controllerId, opponentOfController)))
                    .map(Permanent::getId)
                    .orElse(null);
        }

        // Prefer creature kills when legal, then choose the most threatening one.
        UUID creatureTarget = candidates.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .max(Comparator.comparingDouble(p -> threatScore(gameData, p, controllerId, opponentOfController)))
                .map(Permanent::getId)
                .orElse(null);
        if (creatureTarget != null) {
            return creatureTarget;
        }

        return candidates.getFirst().getId();
    }

    /**
     * Returns the contextual threat score for a creature if a BoardEvaluator is available,
     * otherwise falls back to effective power.
     */
    private double threatScore(GameData gameData, Permanent perm, UUID controllerId, UUID opponentId) {
        if (boardEvaluator != null) {
            return boardEvaluator.creatureThreatScore(gameData, perm, controllerId, opponentId);
        }
        return gameQueryService.getEffectivePower(gameData, perm);
    }

    /**
     * General-purpose target priority for picking an opponent's permanent when the spell's
     * effect type is unknown. Creatures are ranked by contextual threat (lord bonuses,
     * activated abilities, evasion, growth); non-creatures fall back to their mana value
     * as a simple proxy for board impact.
     */
    private double generalTargetPriority(GameData gameData, Permanent perm, UUID controllerId, UUID opponentId) {
        if (gameQueryService.isCreature(gameData, perm)) {
            return threatScore(gameData, perm, controllerId, opponentId);
        }
        return perm.getCard().getManaValue();
    }

    // ===== Activated Ability Targeting =====

    /**
     * Selects the best target for an activated ability. Determines whether the ability
     * is beneficial (targets own permanents) or harmful (targets opponent's permanents)
     * based on its non-cost effects, then finds the best valid target.
     *
     * @return the target UUID, or null if no valid target exists
     */
    UUID chooseAbilityTarget(GameData gameData, ActivatedAbility ability, UUID aiPlayerId, Permanent source) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<CardEffect> nonCostEffects = ability.getEffects().stream()
                .filter(e -> !(e instanceof CostEffect))
                .toList();

        boolean canTargetPlayer = nonCostEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean canTargetPermanent = nonCostEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));

        // Classify: is this ability beneficial to the target or harmful?
        boolean isBeneficial = nonCostEffects.stream().anyMatch(e ->
                (e instanceof CreatureBoostEffect boost
                        && amountEvaluationService.evaluate(gameData, boost.powerBoost(), AmountContext.forEstimation(aiPlayerId)) >= 0)
                        || e instanceof RegenerationEffect
                        || (e instanceof KeywordGrantingEffect grant && grant.scope() == GrantScope.TARGET));

        if (canTargetPermanent) {
            if (isBeneficial) {
                // Target own best creature
                UUID target = findBestOwnCreatureTarget(gameData, ability, aiPlayerId, source);
                if (target != null) return target;
            } else {
                // Target opponent's best creature (for damage, destruction, bounce, tap, etc.)
                UUID target = findBestOpponentTarget(gameData, ability, aiPlayerId, opponentId, nonCostEffects, source);
                if (target != null) return target;
            }
        }

        // For "any target" damage, fall back to opponent's face. One effect allowing a player
        // target doesn't make the player legal for the ability as a whole: a companion effect may
        // require a permanent (Samite Alchemist's "prevent … to target creature you control. Tap
        // that creature."), so the engine gets the final say here too.
        if (canTargetPlayer && opponentId != null
                && isValidAbilityTarget(gameData, ability, opponentId, aiPlayerId, source)) {
            return opponentId;
        }

        return null;
    }

    private UUID findBestOwnCreatureTarget(GameData gameData, ActivatedAbility ability,
                                           UUID aiPlayerId, Permanent source) {
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        return ownBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> !p.getId().equals(source.getId())) // Self-targeting is handled separately
                .filter(p -> isValidAbilityPermanentTarget(gameData, ability, p, aiPlayerId, source))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)
                        + gameQueryService.getEffectiveToughness(gameData, p)))
                .map(Permanent::getId)
                .orElse(null);
    }

    private UUID findBestOpponentTarget(GameData gameData, ActivatedAbility ability,
                                        UUID aiPlayerId, UUID opponentId,
                                        List<CardEffect> effects, Permanent source) {
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        // For damage abilities, prefer creatures we can kill. Amounts evaluate with the
        // ability's source permanent in context (e.g. power/counter-based damage).
        AmountContext amountCtx = new AmountContext(aiPlayerId, source, null, 0, 0);
        for (CardEffect effect : effects) {
            final int damage;
            // Creature-hitting damage (target-creature or any-target); player-only damage
            // (canDamageCreatures() == false) contributes no kill-target search.
            if (effect instanceof DamageDealingEffect dmg && dmg.canDamageCreatures())
                damage = amountEvaluationService.evaluate(gameData, dmg.damageAmount(), amountCtx);
            else damage = 0;

            if (damage > 0) {
                // First try to find a creature we can kill — pick the highest-threat one
                UUID killTarget = oppBattlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> isValidAbilityPermanentTarget(gameData, ability, p, aiPlayerId, source))
                        .filter(p -> gameQueryService.getEffectiveToughness(gameData, p) - p.getMarkedDamage() <= damage)
                        .max(Comparator.comparingDouble(p -> threatScore(gameData, p, opponentId, aiPlayerId)))
                        .map(Permanent::getId)
                        .orElse(null);
                if (killTarget != null) return killTarget;
            }
        }

        // General case: target opponent's highest-threat creature
        return oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> isValidAbilityPermanentTarget(gameData, ability, p, aiPlayerId, source))
                .max(Comparator.comparingDouble(p -> threatScore(gameData, p, opponentId, aiPlayerId)))
                .map(Permanent::getId)
                .orElse(null);
    }

    /**
     * Target pre-filter for activated abilities: runs the engine's own full targeting
     * validation ({@code TargetLegalityService}) against the candidate, so the AI's idea
     * of a legal ability target can never drift from the server's.
     */
    private boolean isValidAbilityPermanentTarget(GameData gameData, ActivatedAbility ability,
                                                  Permanent target, UUID aiPlayerId, Permanent source) {
        return isValidAbilityTarget(gameData, ability, target.getId(), aiPlayerId, source);
    }

    /** {@link #isValidAbilityPermanentTarget} for any target id, permanent or player. */
    private boolean isValidAbilityTarget(GameData gameData, ActivatedAbility ability,
                                         UUID targetId, UUID aiPlayerId, Permanent source) {
        try {
            targetLegalityService.validateActivatedAbilityTargeting(gameData, aiPlayerId, ability,
                    ability.getEffects(), targetId, null, source.getCard(), 0);
            return true;
        } catch (IllegalStateException | IllegalArgumentException e) {
            return false;
        }
    }
}
