package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
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

    private final GameQueryService gameQueryService;
    private final TargetValidationService targetValidationService;
    private final TargetLegalityService targetLegalityService;
    private final ValidTargetService validTargetService;
    private final BoardEvaluator boardEvaluator;

    AiTargetSelector(GameQueryService gameQueryService, TargetValidationService targetValidationService,
                     TargetLegalityService targetLegalityService) {
        this(gameQueryService, targetValidationService, targetLegalityService, null);
    }

    AiTargetSelector(GameQueryService gameQueryService, TargetValidationService targetValidationService,
                     TargetLegalityService targetLegalityService, BoardEvaluator boardEvaluator) {
        this.gameQueryService = gameQueryService;
        this.targetValidationService = targetValidationService;
        this.targetLegalityService = targetLegalityService;
        this.validTargetService = new ValidTargetService(gameQueryService);
        this.boardEvaluator = boardEvaluator;
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

        // Handle destroy/exile removal effects (ETB creatures or removal spells)
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof DestroyTargetPermanentEffect
                    || effect instanceof ExileTargetPermanentEffect) {
                return chooseDestroyTarget(gameData, card, aiPlayerId, opponentId);
            }
        }
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof DestroyTargetPermanentEffect
                    || effect instanceof ExileTargetPermanentEffect) {
                return chooseDestroyTarget(gameData, card, aiPlayerId, opponentId);
            }
        }

        boolean isBeneficial = false;
        if (card.isAura()) {
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if ((effect instanceof StaticBoostEffect boost
                        && (boost.scope() == GrantScope.ENCHANTED_CREATURE || boost.scope() == GrantScope.EQUIPPED_CREATURE))
                        || (effect instanceof GrantKeywordEffect grant && grant.scope() == GrantScope.ENCHANTED_CREATURE)) {
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

        // General fallback: find any valid target using target filter + effect validators
        // Search own battlefield first (for beneficial ETB effects like Awakener Druid)
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        for (Permanent p : ownBattlefield) {
            if (isValidPermanentTarget(gameData, card, p, aiPlayerId)) {
                return p.getId();
            }
        }
        // Then search opponent battlefield — prefer the most threatening valid target so that
        // damage, bounce, tap, and similar "soft" removal spells still attack the real threat
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
            double value = evaluateSpellOnStack(gameData, entry, aiPlayerId);
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
    private double evaluateSpellOnStack(GameData gameData, StackEntry entry, UUID aiPlayerId) {
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
     * Selects targets for multi-target spells (cards with more than one SpellTarget group).
     * Returns a list of target UUIDs (one per satisfied target group), or null if
     * mandatory targets cannot be satisfied.
     */
    List<UUID> chooseMultiTargets(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<SpellTarget> spellTargets = card.getSpellTargets();
        List<UUID> result = new ArrayList<>();
        Set<UUID> alreadyChosen = new HashSet<>();

        for (SpellTarget st : spellTargets) {
            List<CardEffect> groupEffects = findEffectsForTargetGroup(card, st.getIndex());

            boolean wantsPlayer = groupEffects.stream().anyMatch(CardEffect::canTargetPlayer);
            boolean wantsPermanent = groupEffects.stream().anyMatch(CardEffect::canTargetPermanent)
                    || st.getFilter() != null;

            UUID chosen = null;

            if (wantsPlayer && !wantsPermanent) {
                chosen = pickPlayerTargetForGroup(gameData, aiPlayerId, opponentId, groupEffects);
            } else if (wantsPermanent) {
                chosen = pickPermanentTargetForGroup(gameData, card, aiPlayerId, opponentId, st, alreadyChosen);
            }

            if (chosen != null) {
                result.add(chosen);
                alreadyChosen.add(chosen);
            } else if (st.getMinTargets() > 0) {
                return null; // Mandatory target cannot be satisfied
            }
        }

        return result.isEmpty() ? null : result;
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
                                           List<CardEffect> effects) {
        boolean isBeneficial = effects.stream().anyMatch(ExtraTurnEffect.class::isInstance);

        UUID preferred = isBeneficial ? aiPlayerId : opponentId;
        UUID fallback = isBeneficial ? opponentId : aiPlayerId;

        if (preferred != null && !gameQueryService.playerHasShroud(gameData, preferred)
                && (isBeneficial || !gameQueryService.playerHasHexproof(gameData, preferred))) {
            return preferred;
        }
        if (fallback != null && !gameQueryService.playerHasShroud(gameData, fallback)
                && (!isBeneficial || !gameQueryService.playerHasHexproof(gameData, fallback))) {
            return fallback;
        }
        return null;
    }

    /**
     * Picks a permanent target for a specific multi-target group, using the group's filter.
     * Searches opponent's battlefield first (more likely target for harmful effects), picking
     * the most threatening valid target. Falls back to the AI's own battlefield if nothing on
     * the opponent's side is legal (e.g. when the target group only accepts own permanents).
     */
    private UUID pickPermanentTargetForGroup(GameData gameData, Card card, UUID aiPlayerId,
                                              UUID opponentId, SpellTarget st, Set<UUID> alreadyChosen) {
        TargetFilter groupFilter = st.getFilter();
        for (UUID playerId : new UUID[]{opponentId, aiPlayerId}) {
            if (playerId == null) continue;
            boolean searchingOpponent = playerId.equals(opponentId);
            UUID best = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                    .filter(p -> !alreadyChosen.contains(p.getId()))
                    .filter(p -> validTargetService.isValidMultiTargetPermanent(gameData, card, p, aiPlayerId, groupFilter))
                    .max(Comparator.comparingDouble(p -> searchingOpponent
                            ? generalTargetPriority(gameData, p, opponentId, aiPlayerId)
                            : generalTargetPriority(gameData, p, aiPlayerId, opponentId)))
                    .map(Permanent::getId)
                    .orElse(null);
            if (best != null) {
                return best;
            }
        }
        return null;
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
            if (e instanceof ReplacementConditionalEffect replacement) {
                effectToCheck = replacement.baseEffect();
            }
            if (effectToCheck.canTargetPlayer()) result.add(TargetType.PLAYER);
            if (effectToCheck.canTargetPermanent()) result.add(TargetType.PERMANENT);
            if (effectToCheck.canTargetSpell()) result.add(TargetType.SPELL_ON_STACK);
            if (effectToCheck.canTargetGraveyard()) result.add(TargetType.GRAVEYARD);
            if (effectToCheck.canTargetExile()) result.add(TargetType.EXILE);
        }
        for (CardEffect e : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (e.canTargetPlayer()) result.add(TargetType.PLAYER);
            if (e.canTargetPermanent()) result.add(TargetType.PERMANENT);
        }
        return result;
    }

    private UUID chooseDestroyTarget(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId) {
        // Search opponent's battlefield first
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        UUID oppTarget = findDestroyCandidate(gameData, card, oppBattlefield, aiPlayerId);
        if (oppTarget != null) {
            return oppTarget;
        }

        // Fall back to own battlefield
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        return findDestroyCandidate(gameData, card, ownBattlefield, aiPlayerId);
    }

    /**
     * Returns all valid permanent targets for an X spell whose target filter
     * includes {@link com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate},
     * filtered to those with mana value between 1 and maxAffordableX (inclusive).
     */
    List<Permanent> findValidPermanentTargetsForManaValueX(GameData gameData, Card card,
                                                            UUID aiPlayerId, int maxAffordableX) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<Permanent> result = new ArrayList<>();
        // Search opponent's battlefield first (more likely target for steal effects)
        for (UUID playerId : new UUID[]{opponentId, aiPlayerId}) {
            if (playerId == null) continue;
            for (Permanent p : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                int mv = p.getCard().getManaValue();
                if (mv >= 1 && mv <= maxAffordableX && isValidPermanentTarget(gameData, card, p, aiPlayerId)) {
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
            if (!effect.canTargetGraveyard()) continue;

            List<Card> candidates;
            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                candidates = getGraveyardCandidates(gameData, rge.source(), aiPlayerId, opponentId);
                if (rge.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> gameQueryService.matchesCardPredicate(c, rge.filter(), card.getId()))
                            .toList();
                }
                if (rge.requiresManaValueEqualsX() && maxAffordableX < Integer.MAX_VALUE) {
                    candidates = candidates.stream()
                            .filter(c -> c.getManaValue() >= 1 && c.getManaValue() <= maxAffordableX)
                            .toList();
                }
            } else {
                // For non-return effects: canTargetAnyGraveyard → all graveyards, otherwise → opponent's
                GraveyardSearchScope scope = effect.canTargetAnyGraveyard()
                        ? GraveyardSearchScope.ALL_GRAVEYARDS
                        : GraveyardSearchScope.OPPONENT_GRAVEYARD;
                candidates = getGraveyardCandidates(gameData, scope, aiPlayerId, opponentId);

                // Apply card-type filters matching what GraveyardTargetValidators enforces
                if (effect instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect) {
                    candidates = candidates.stream().filter(c -> c.hasType(CardType.CREATURE)).toList();
                } else if (effect instanceof CastTargetInstantOrSorceryFromGraveyardEffect) {
                    candidates = candidates.stream()
                            .filter(c -> c.hasType(CardType.INSTANT) || c.hasType(CardType.SORCERY)).toList();
                } else if (effect instanceof ExileTargetCardFromGraveyardEffect e && e.requiredType() != null) {
                    candidates = candidates.stream().filter(c -> c.hasType(e.requiredType())).toList();
                } else if (effect instanceof GrantFlashbackToTargetGraveyardCardEffect e) {
                    candidates = candidates.stream()
                            .filter(c -> e.cardTypes().stream().anyMatch(c::hasType)).toList();
                } else if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect e && e.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> gameQueryService.matchesCardPredicate(c, e.filter(), card.getId())).toList();
                } else if (effect instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect) {
                    candidates = candidates.stream()
                            .filter(c -> c.hasType(CardType.ARTIFACT) || c.hasType(CardType.CREATURE)).toList();
                } else if (effect instanceof ExileTargetGraveyardCardAndSameNameFromZonesEffect) {
                    candidates = candidates.stream()
                            .filter(c -> !(c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))).toList();
                }
            }

            if (!candidates.isEmpty()) {
                return new ArrayList<>(candidates);
            }
        }
        return List.of();
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
     * Builds a damage assignment map for divided damage spells (e.g. Ignite Disorder, Fight with Fire kicked).
     * Distributes damage to maximize creature kills on the opponent's battlefield.
     * For "any targets" effects, dumps remaining damage on the opponent player.
     * Returns null if no valid targets exist.
     */
    Map<UUID, Integer> buildDamageAssignments(GameData gameData, Card card, UUID aiPlayerId) {
        DealDividedDamageAmongTargetCreaturesEffect creaturesEffect = card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e instanceof DealDividedDamageAmongTargetCreaturesEffect)
                .map(DealDividedDamageAmongTargetCreaturesEffect.class::cast)
                .findFirst()
                .orElse(null);

        DealDividedDamageAmongAnyTargetsEffect anyTargetEffect = findDividedDamageAnyTargetsEffect(card);

        if (creaturesEffect == null && anyTargetEffect == null) {
            // X-damage divided among attacking creatures — only relevant during combat
            return null;
        }

        int totalDamage;
        boolean canTargetPlayers;
        int maxTargets;
        if (creaturesEffect != null) {
            totalDamage = creaturesEffect.totalDamage();
            canTargetPlayers = false;
            maxTargets = Math.max(1, card.getMaxTargets());
        } else {
            totalDamage = anyTargetEffect.totalDamage();
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
     * Searches for a DealDividedDamageAmongAnyTargetsEffect in the card's spell effects,
     * including inside KickerReplacementEffect wrappers.
     */
    private DealDividedDamageAmongAnyTargetsEffect findDividedDamageAnyTargetsEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof DealDividedDamageAmongAnyTargetsEffect anyTarget) {
                return anyTarget;
            }
            if (effect instanceof ReplacementConditionalEffect replacement
                    && replacement.upgradedEffect() instanceof DealDividedDamageAmongAnyTargetsEffect anyTarget) {
                return anyTarget;
            }
        }
        return null;
    }

    private UUID findDestroyCandidate(GameData gameData, Card card, List<Permanent> battlefield, UUID aiPlayerId) {
        List<Permanent> candidates = battlefield.stream()
                .filter(p -> isValidPermanentTarget(gameData, card, p, aiPlayerId))
                .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        UUID controllerId = !candidates.isEmpty()
                ? gameQueryService.findPermanentController(gameData, candidates.getFirst().getId())
                : null;
        UUID opponentOfController = controllerId != null
                ? AiUtils.getOpponentId(gameData, controllerId)
                : null;

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

        boolean canTargetPlayer = nonCostEffects.stream().anyMatch(CardEffect::canTargetPlayer);
        boolean canTargetPermanent = nonCostEffects.stream().anyMatch(CardEffect::canTargetPermanent);

        // Classify: is this ability beneficial to the target or harmful?
        boolean isBeneficial = nonCostEffects.stream().anyMatch(e ->
                (e instanceof BoostTargetCreatureEffect boost && boost.powerBoost() >= 0)
                        || e instanceof RegenerateEffect
                        || (e instanceof GrantKeywordEffect grant && grant.scope() == GrantScope.TARGET));

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

        // For "any target" damage, fall back to opponent's face
        if (canTargetPlayer) {
            if (opponentId != null
                    && !gameQueryService.playerHasShroud(gameData, opponentId)
                    && !gameQueryService.playerHasHexproof(gameData, opponentId)) {
                return opponentId;
            }
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

        // For damage abilities, prefer creatures we can kill
        for (CardEffect effect : effects) {
            final int damage;
            if (effect instanceof DealDamageToAnyTargetEffect dmg) damage = dmg.damage();
            else if (effect instanceof DealDamageToTargetCreatureEffect dmg) damage = dmg.damage();
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
     * Simplified target validation for activated abilities. Checks the ability's
     * TargetFilter, hexproof, shroud, and protection. The server performs full
     * validation, so this is a best-effort pre-filter.
     */
    private boolean isValidAbilityPermanentTarget(GameData gameData, ActivatedAbility ability,
                                                  Permanent target, UUID aiPlayerId, Permanent source) {
        // Hexproof check
        UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetController != null && !targetController.equals(aiPlayerId)) {
            if (gameQueryService.hasKeyword(gameData, target, com.github.laxika.magicalvibes.model.Keyword.HEXPROOF)) {
                return false;
            }
            if (gameQueryService.hasKeyword(gameData, target, com.github.laxika.magicalvibes.model.Keyword.SHROUD)) {
                return false;
            }
        }
        if (gameQueryService.hasKeyword(gameData, target, com.github.laxika.magicalvibes.model.Keyword.SHROUD)) {
            return false;
        }

        // TargetFilter check
        TargetFilter filter = ability.getTargetFilter();
        if (filter != null) {
            FilterContext ctx = FilterContext.of(gameData)
                    .withSourceControllerId(aiPlayerId)
                    .withSourceCardId(source.getCard().getId());
            if (filter instanceof PermanentPredicateTargetFilter ppf) {
                if (!gameQueryService.matchesPermanentPredicate(target, ppf.predicate(), ctx)) {
                    return false;
                }
            } else if (filter instanceof ControlledPermanentPredicateTargetFilter cpf) {
                if (!targetController.equals(aiPlayerId)) return false;
                if (!gameQueryService.matchesPermanentPredicate(target, cpf.predicate(), ctx)) {
                    return false;
                }
            } else if (filter instanceof OwnedPermanentPredicateTargetFilter opf) {
                if (!gameQueryService.matchesPermanentPredicate(target, opf.predicate(), ctx)) {
                    return false;
                }
            }
        }

        return true;
    }
}
