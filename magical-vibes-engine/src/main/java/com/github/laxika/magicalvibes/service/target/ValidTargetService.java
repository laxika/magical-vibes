package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PlayerDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ValidTargetService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;
    private final TargetValidationService targetValidationService;

    @Autowired
    public ValidTargetService(GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService,
                              TargetLegalityService targetLegalityService,
                              TargetValidationService targetValidationService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.targetLegalityService = targetLegalityService;
        this.targetValidationService = targetValidationService;
    }

    /**
     * Legacy 2-arg constructor for contexts that only enumerate player targets for triggers
     * (e.g. StepTriggerService's upkeep pipeline) and never route a permanent through the shared
     * spell-target core. The structural and validator collaborators are left null; calling any
     * permanent-legality method on such an instance would NPE by design.
     */
    public ValidTargetService(GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService) {
        this(gameQueryService, predicateEvaluationService, null, null);
    }

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds) {
        return computeValidTargetsForSpell(gameData, card, controllerId, alreadySelectedIds, null, null);
    }

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds, Integer xValue) {
        return computeValidTargetsForSpell(gameData, card, controllerId, alreadySelectedIds, xValue, null);
    }

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds, Integer xValue, Boolean kicked) {
        boolean isMultiTarget = card.getMaxTargets() > 1;

        // For modal spells (and modal ETB creatures) the request's xValue carries the encoded
        // mode selection; resolve to the chosen mode's effects so targeting reflects that mode.
        ChooseOneEffect.ChooseOneOption chosenMode = findChosenMode(card, xValue);
        Integer modeSelection = chosenMode != null || hasModalEffect(card) && xValue != null ? xValue : null;

        List<CardEffect> spellEffects = card.getEffects(EffectSlot.SPELL);
        List<CardEffect> etbEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD);
        TargetFilter modeFilter = chosenMode != null ? chosenMode.targetFilter() : null;
        Set<TargetType> allowedTargets;
        if (kicked != null || modeSelection != null) {
            spellEffects = EffectResolution.resolveEffects(spellEffects, kicked, modeSelection);
            if (modeSelection != null) {
                etbEffects = EffectResolution.resolveEffects(etbEffects, kicked, modeSelection);
            }
            allowedTargets = EffectResolution.computeAllowedTargets(
                    spellEffects, etbEffects, card.isAura(), card.isEnchantPlayer());
        } else {
            allowedTargets = EffectResolution.computeAllowedTargets(card);
        }

        List<UUID> validPermanentIds = new ArrayList<>();
        List<UUID> validPlayerIds = new ArrayList<>();
        List<UUID> validGraveyardCardIds = new ArrayList<>();
        Set<UUID> excludeIds = alreadySelectedIds != null ? Set.copyOf(alreadySelectedIds) : Set.of();

        int positionIndex = alreadySelectedIds != null ? alreadySelectedIds.size() : 0;

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            // Determine per-position filter for multi-target spells; a chosen mode's
            // filter override plays the same role for modal spells.
            TargetFilter positionFilter = isMultiTarget && positionIndex < card.getMultiTargetFilters().size()
                    ? card.getMultiTargetFilters().get(positionIndex)
                    : modeFilter;

            List<CardEffect> effectiveSpellEffects = spellEffects;
            gameData.forEachPermanent((playerId, perm) -> {
                if (excludeIds.contains(perm.getId())) return;
                if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter, effectiveSpellEffects)) {
                    validPermanentIds.add(perm.getId());
                }
            });

            // Cross-target restriction (e.g. Rivals' Duel): once a creature is chosen, later
            // positions may not choose a creature that shares a creature type with it.
            if (card.getMultiTargetConstraint() == MultiTargetConstraint.SHARE_NO_CREATURE_TYPES && !excludeIds.isEmpty()) {
                List<Permanent> selected = excludeIds.stream()
                        .map(id -> gameQueryService.findPermanentById(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                validPermanentIds.removeIf(id -> {
                    Permanent perm = gameQueryService.findPermanentById(gameData, id);
                    return perm != null && selected.stream()
                            .anyMatch(sel -> gameQueryService.shareCreatureType(gameData, sel, perm));
                });
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            boolean positionAllowsPlayers;
            if (!isMultiTarget) {
                positionAllowsPlayers = true;
            } else {
                positionAllowsPlayers = card.doesPositionAllowPlayerTargets(positionIndex);
            }

            if (positionAllowsPlayers) {
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, modeFilter != null ? modeFilter : card.getTargetFilter(), playerId, controllerId)) {
                        validPlayerIds.add(playerId);
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            validGraveyardCardIds.addAll(computeValidGraveyardTargets(gameData, card, spellEffects, controllerId, xValue));
        }

        String prompt = "Select a target for " + card.getName();
        if (isMultiTarget) {
            prompt = "Select targets for " + card.getName();
        }

        int responseMinTargets = card.getMinTargets();
        int responseMaxTargets = card.getMaxTargets();
        if (card.hasXScaledTargets()) {
            int effectiveX = xValue != null ? xValue : 0;
            responseMinTargets = card.getEffectiveMinTargets(effectiveX);
            responseMaxTargets = card.getEffectiveMaxTargets(effectiveX);
        }
        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, validGraveyardCardIds, responseMinTargets, responseMaxTargets, prompt);
    }

    /** Finds the card's modal effect in the SPELL or ON_ENTER_BATTLEFIELD slot, if any. */
    private ChooseOneEffect findModalEffect(Card card) {
        for (CardEffect e : card.getEffects(EffectSlot.SPELL)) {
            if (e instanceof ChooseOneEffect coe) return coe;
        }
        for (CardEffect e : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (e instanceof ChooseOneEffect coe) return coe;
        }
        return null;
    }

    private boolean hasModalEffect(Card card) {
        return findModalEffect(card) != null;
    }

    /**
     * Resolves the single chosen mode of a modal card from the request's encoded xValue.
     * Returns null for non-modal cards, missing/skip ({@code < 0}) selections, and
     * choose-multiple selections (whose modes may not declare per-mode filters).
     */
    private ChooseOneEffect.ChooseOneOption findChosenMode(Card card, Integer xValue) {
        if (xValue == null) return null;
        ChooseOneEffect modal = findModalEffect(card);
        if (modal == null || modal.choicesRequired() != 1) return null;
        if (xValue < 0 || xValue >= modal.options().size()) return null;
        return modal.options().get(xValue);
    }

    public ValidTargetsResponse computeValidTargetsForAbility(GameData gameData, Card sourceCard, ActivatedAbility ability, UUID controllerId, int permanentIndex) {
        return computeValidTargetsForAbility(gameData, sourceCard, ability, controllerId, permanentIndex, List.of());
    }

    public ValidTargetsResponse computeValidTargetsForAbility(GameData gameData, Card sourceCard, ActivatedAbility ability, UUID controllerId, int permanentIndex, List<UUID> alreadySelectedIds) {
        List<UUID> validPermanentIds = new ArrayList<>();
        List<UUID> validPlayerIds = new ArrayList<>();
        Set<UUID> excludeIds = alreadySelectedIds != null && !alreadySelectedIds.isEmpty() ? Set.copyOf(alreadySelectedIds) : Set.of();

        if (ability.isMultiTarget()) {
            // Multi-target ability: use per-position filter
            int positionIndex = alreadySelectedIds != null ? alreadySelectedIds.size() : 0;
            TargetFilter positionFilter = positionIndex < ability.getMultiTargetFilters().size()
                    ? ability.getMultiTargetFilters().get(positionIndex)
                    : null;

            if (positionFilter instanceof PlayerPredicateTargetFilter) {
                // Player-targeting position: add valid players
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, ability.getTargetFilter(), playerId, controllerId)) {
                        validPlayerIds.add(playerId);
                    }
                }
            } else {
                gameData.forEachPermanent((playerId, perm) -> {
                    if (excludeIds.contains(perm.getId())) return;
                    if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, false, permanentIndex, positionFilter)) {
                        validPermanentIds.add(perm.getId());
                    }
                });
            }

            String prompt = "Select targets for " + sourceCard.getName() + " ability";
            return new ValidTargetsResponse(validPermanentIds, validPlayerIds, ability.getMinTargets(), ability.getMaxTargets(), prompt);
        }

        boolean targetsPlayer = ability.getEffects().stream().anyMatch(e -> e.targetSpec().category().includesPlayers());
        boolean targetsPermanent = ability.getEffects().stream().anyMatch(e -> e.targetSpec().category().includesPermanents());
        boolean targetsGraveyard = ability.getEffects().stream().anyMatch(e -> e.targetSpec().category().isGraveyard());
        boolean targetsBlockingThis = ability.getEffects().stream()
                .anyMatch(e -> e instanceof DestroyCreatureBlockingThisEffect);

        if (targetsPermanent) {
            gameData.forEachPermanent((playerId, perm) -> {
                if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, targetsBlockingThis, permanentIndex, null)) {
                    validPermanentIds.add(perm.getId());
                }
            });
        }

        if (targetsPlayer) {
            for (UUID playerId : gameData.playerIds) {
                if (isValidPlayerTarget(gameData, ability.getTargetFilter(), playerId, controllerId)) {
                    validPlayerIds.add(playerId);
                }
            }
        }

        List<UUID> validGraveyardCardIds = new ArrayList<>();
        if (targetsGraveyard) {
            validGraveyardCardIds.addAll(computeValidGraveyardTargetsForAbility(gameData, ability, controllerId, excludeIds));
        }

        int minTargets = 1;
        int maxTargets = 1;
        String prompt = "Select a target for " + sourceCard.getName() + " ability";

        // Multi-target graveyard ability (e.g. "exile two target cards")
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof ExileGraveyardCardsEffect graveyardEffect
                    && graveyardEffect.scope() == GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD) {
                minTargets = graveyardEffect.count();
                maxTargets = graveyardEffect.count();
                prompt = "Select " + graveyardEffect.count() + " target cards from an opponent's graveyard";
                break;
            }
        }

        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, validGraveyardCardIds, minTargets, maxTargets, prompt);
    }

    /**
     * Full permanent-target validation for a spell — the same logic used by
     * {@link #computeValidTargetsForSpell} (and therefore the frontend UI).
     * Includes protection/hexproof/shroud, the spell's TargetFilter, and the
     * "any target = creature/planeswalker/player" restriction.
     */
    public boolean isValidSpellPermanentTarget(GameData gameData, Card card, Permanent perm, UUID controllerId) {
        boolean isMultiTarget = card.getMaxTargets() > 1;
        return isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, null);
    }

    /**
     * Validates a permanent as a target for a specific position in a multi-target spell.
     * Uses the given positionFilter instead of the card's global targetFilter, so each
     * target group's filter is checked independently.
     */
    public boolean isValidMultiTargetPermanent(GameData gameData, Card card, Permanent perm, UUID controllerId, TargetFilter positionFilter) {
        return isValidPermanentTarget(gameData, card, perm, controllerId, true, positionFilter);
    }

    /**
     * Checks whether a permanent can legally be targeted by a spell cast by the given controller.
     * Evaluates shroud, hexproof, CantBeTargetOfSpellsOrAbilities, protection from color,
     * protection from card types, cant-be-targeted-by-spell-color, and the spell's TargetFilter.
     */
    public boolean canPermanentBeTargetedBySpell(GameData gameData, Permanent perm, Card spellCard, UUID castingPlayerId) {
        // Structural targeting rules (protection, can't-be-targeted, shroud, hexproof, hexproof-from-color)
        // are owned by the shared spell-target core; enumeration adds only the card's TargetFilter.
        if (!targetLegalityService.checkSpellPermanentTargetableReason(gameData, perm, spellCard, castingPlayerId).isEmpty()) {
            return false;
        }

        // Card's TargetFilter
        if (!passesTargetFilter(gameData, spellCard.getTargetFilter(), perm, spellCard.getId(), castingPlayerId)) {
            return false;
        }

        return true;
    }

    private boolean isValidPermanentTarget(GameData gameData, Card card, Permanent perm, UUID controllerId,
                                            boolean isMultiTarget, TargetFilter positionFilter) {
        return isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter,
                card.getEffects(EffectSlot.SPELL));
    }

    private boolean isValidPermanentTarget(GameData gameData, Card card, Permanent perm, UUID controllerId,
                                            boolean isMultiTarget, TargetFilter positionFilter,
                                            List<CardEffect> spellEffects) {
        // For multi-target spells with per-position filters, use protection/hexproof checks
        // but skip the global targetFilter from canPermanentBeTargetedBySpell, since the
        // per-position filter below handles type restriction for each target group.
        if (positionFilter != null) {
            if (!canPermanentBeTargetedBySpellCore(gameData, perm, card, controllerId)) {
                return false;
            }
        } else {
            if (!canPermanentBeTargetedBySpell(gameData, perm, card, controllerId)) {
                return false;
            }
        }

        // Per-position filter for multi-target spells
        if (!passesTargetFilter(gameData, positionFilter, perm, card.getId(), controllerId)) {
            return false;
        }

        // "Any target" in MTG means creature, planeswalker, or player — not all permanents.
        // When all permanent-targeting spell effects also target players (i.e. "any target"),
        // restrict valid permanent targets to creatures and planeswalkers.
        if (card.getTargetFilter() == null && positionFilter == null) {
            List<CardEffect> permanentEffects = spellEffects.stream()
                    .filter(e -> e.targetSpec().category().includesPermanents())
                    .toList();
            boolean allAnyTarget = !permanentEffects.isEmpty()
                    && permanentEffects.stream().allMatch(e -> e.targetSpec().category().includesPlayers());
            if (allAnyTarget) {
                if (!gameQueryService.isCreature(gameData, perm) && !isPlaneswalker(perm)) {
                    return false;
                }
            } else if (isMultiTarget && !gameQueryService.isCreature(gameData, perm)) {
                return false;
            }
        }

        // Per-effect @ValidatesTarget validators — the same type-narrowing the single-target cast
        // path (TargetLegalityService.checkSpellTargeting) applies. Running them here keeps UI/AI
        // enumeration from offering a permanent that cast-time validation would reject (e.g. a land
        // for a filterless "target creature" spell such as Wrack with Madness). Scoped to the
        // single-target case; multi-target positions are governed by their per-position TargetFilter
        // (validators for multi-target effects are intentionally out of scope — see refactor step 3).
        if (positionFilter == null && !isMultiTarget
                && targetValidationService.checkEffectTargets(spellEffects,
                        new TargetValidationContext(gameData, perm.getId(), null, card)).isPresent()) {
            return false;
        }

        return true;
    }

    /**
     * Core targeting checks (protection, hexproof, shroud) without the global TargetFilter check.
     * Used by multi-target spells where per-position filters handle type restriction.
     */
    private boolean canPermanentBeTargetedBySpellCore(GameData gameData, Permanent perm, Card spellCard, UUID castingPlayerId) {
        return targetLegalityService.checkSpellPermanentTargetableReason(gameData, perm, spellCard, castingPlayerId).isEmpty();
    }

    /**
     * Filters a list of candidate players down to those legal under the given target filter.
     * Used by the upkeep player-target pipeline so that "target opponent" triggers
     * (e.g. Nath of the Gilt-Leaf) do not offer the controller as a valid target.
     * A {@code null} filter leaves the candidates unrestricted (e.g. Bloodgift Demon's "target player").
     */
    public List<UUID> filterValidPlayerTargets(GameData gameData, TargetFilter targetFilter, List<UUID> candidates, UUID controllerId) {
        List<UUID> result = new ArrayList<>();
        for (UUID playerId : candidates) {
            if (isValidPlayerTarget(gameData, targetFilter, playerId, controllerId)) {
                result.add(playerId);
            }
        }
        return result;
    }

    private boolean isValidPlayerTarget(GameData gameData, TargetFilter targetFilter, UUID playerId, UUID controllerId) {
        // Player shroud
        if (gameQueryService.playerHasShroud(gameData, playerId)) {
            return false;
        }

        // Player hexproof (only blocks opponents)
        if (!controllerId.equals(playerId) && gameQueryService.playerHasHexproof(gameData, playerId)) {
            return false;
        }

        // PlayerPredicateTargetFilter (e.g. "target opponent")
        if (targetFilter instanceof PlayerPredicateTargetFilter playerFilter
                && !matchesPlayerPredicate(gameData, controllerId, playerId, playerFilter.predicate())) {
            return false;
        }

        // Any-target restriction: the player side is checked against the player predicate.
        if (targetFilter instanceof AnyTargetPredicateTargetFilter anyFilter
                && !matchesPlayerPredicate(gameData, controllerId, playerId, anyFilter.playerPredicate())) {
            return false;
        }

        return true;
    }

    private boolean matchesPlayerPredicate(GameData gameData, UUID controllerId, UUID playerId, PlayerPredicate predicate) {
        return switch (predicate) {
            case PlayerRelationPredicate rel -> switch (rel.relation()) {
                case ANY -> true;
                case SELF -> controllerId.equals(playerId);
                case OPPONENT -> !controllerId.equals(playerId);
            };
            case PlayerDealtDamageThisTurnPredicate ignored -> gameData.playersDealtDamageThisTurn.contains(playerId);
        };
    }

    private boolean isValidAbilityPermanentTarget(GameData gameData, Card sourceCard, ActivatedAbility ability,
                                                   Permanent perm, UUID controllerId,
                                                   boolean targetsBlockingThis, int sourcePermanentIndex,
                                                   TargetFilter positionFilter) {
        // Special case: targeting blocking creature
        if (targetsBlockingThis) {
            if (!gameQueryService.isCreature(gameData, perm) || !perm.isBlocking()) {
                return false;
            }
            if (!perm.getBlockingTargets().contains(sourcePermanentIndex)) {
                return false;
            }
        }

        if (isBlockedByHexproofOrGrantedEffect(gameData, perm, controllerId)) {
            return false;
        }

        // Can't be the target of opponents' abilities (e.g. Shanna, Sisay's Legacy)
        if (gameQueryService.cantBeTargetOfOpponentAbilities(gameData, perm)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return false;
            }
        }

        // Hexproof from color (blocks opponent's abilities of the specified color)
        if (isBlockedByHexproofFromColor(gameData, perm, sourceCard != null ? sourceCard.getColor() : null, controllerId)) {
            return false;
        }

        // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, perm, sourceCard)) {
            return false;
        }

        // TargetFilter from ability
        if (!passesTargetFilter(gameData, ability.getTargetFilter(), perm, sourceCard.getId(), controllerId)) {
            return false;
        }

        // Protection from source color/type/subtype (for abilities that deal damage or destroy)
        boolean dealsDamageOrDestroys = ability.getEffects().stream().anyMatch(e ->
                e.targetSpec().category().includesPermanents() && e.targetSpec().harmful());
        if (dealsDamageOrDestroys) {
            if (sourceCard.getColor() != null && gameQueryService.hasProtectionFrom(gameData, perm, sourceCard.getColor())) {
                return false;
            }
            if (gameQueryService.hasProtectionFromSourceCardTypes(perm, sourceCard)) {
                return false;
            }
            if (gameQueryService.hasProtectionFromSourceSubtypes(perm, sourceCard)) {
                return false;
            }
        }

        // Per-position filter for multi-target abilities
        if (!passesTargetFilter(gameData, positionFilter, perm, sourceCard.getId(), controllerId)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if at least one legal target exists for the given spell.
     * Per MTG rule 601.2c, a spell can't be cast unless a legal set of targets can be chosen for it.
     */
    public boolean hasValidTargetsForSpell(GameData gameData, Card card, UUID controllerId) {
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);
        boolean isMultiTarget = card.getMaxTargets() > 1;

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent perm : battlefield) {
                    if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, null)) {
                        return true;
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            // Check if any position allows player targets (position 0 for hasValidTargets check)
            boolean anyPositionAllowsPlayers;
            if (!isMultiTarget) {
                anyPositionAllowsPlayers = true;
            } else {
                anyPositionAllowsPlayers = card.doesPositionAllowPlayerTargets(0);
            }

            if (anyPositionAllowsPlayers) {
                for (UUID playerId : gameData.playerIds) {
                    if (isValidPlayerTarget(gameData, card.getTargetFilter(), playerId, controllerId)) {
                        return true;
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.SPELL_ON_STACK)) {
            // A "spell or permanent" targeter (e.g. Glamerdye) is castable when a spell is on the
            // stack even if no permanent is available. The per-spell target filter is enforced at
            // cast time; here it is enough that any spell (not an ability) is present.
            boolean anySpellOnStack = gameData.stack.stream()
                    .anyMatch(se -> se.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                            && se.getEntryType() != StackEntryType.ACTIVATED_ABILITY);
            if (anySpellOnStack) {
                return true;
            }
        }

        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            return true;
        }

        if (allowedTargets.contains(TargetType.EXILE)) {
            return true;
        }

        return false;
    }

    /**
     * Computes valid graveyard card targets for a spell. Handles scope filtering (controller's
     * graveyard, opponent's, or all), card predicate filtering, and X-value mana value matching.
     */
    private List<UUID> computeValidGraveyardTargets(GameData gameData, Card card, List<CardEffect> spellEffects, UUID controllerId, Integer xValue) {
        int effectiveXValue = xValue != null ? xValue : 0;
        List<UUID> validIds = new ArrayList<>();

        for (CardEffect effect : spellEffects) {
            if (!effect.targetSpec().category().isGraveyard()) continue;

            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                List<UUID> searchPlayerIds = switch (rge.source()) {
                    case CONTROLLERS_GRAVEYARD -> List.of(controllerId);
                    case OPPONENT_GRAVEYARD -> gameData.orderedPlayerIds.stream()
                            .filter(id -> !id.equals(controllerId)).toList();
                    case ALL_GRAVEYARDS -> gameData.orderedPlayerIds;
                };

                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (rge.filter() != null && !predicateEvaluationService.matchesCardPredicate(c, rge.filter(), card.getId())) {
                            continue;
                        }
                        if (rge.requiresManaValueEqualsX() && c.getManaValue() != effectiveXValue) {
                            continue;
                        }
                        validIds.add(c.getId());
                    }
                }
            } else {
                // Generic graveyard-targeting effects (e.g. PutCreatureFromOpponentGraveyard)
                boolean anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                List<UUID> searchPlayerIds = anyGraveyard
                        ? gameData.orderedPlayerIds
                        : gameData.orderedPlayerIds.stream().filter(id -> !id.equals(controllerId)).toList();

                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (!matchesGraveyardEffectTypeFilter(effect, c, card.getId())) continue;
                        validIds.add(c.getId());
                    }
                }
            }

            if (!validIds.isEmpty()) break;
        }

        return validIds;
    }

    private List<UUID> computeValidGraveyardTargetsForAbility(GameData gameData, ActivatedAbility ability,
                                                                UUID controllerId, Set<UUID> excludeIds) {
        List<UUID> validIds = new ArrayList<>();

        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof ExileGraveyardCardsEffect ge
                    && ge.scope() == GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD) {
                // Opponent-only graveyard targeting
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (playerId.equals(controllerId)) continue;
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (!excludeIds.contains(c.getId())) {
                            validIds.add(c.getId());
                        }
                    }
                }
                break;
            }
            if (effect.targetSpec().category().isGraveyard()) {
                boolean anyGraveyard = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;
                List<UUID> searchPlayerIds = anyGraveyard
                        ? gameData.orderedPlayerIds
                        : gameData.orderedPlayerIds.stream().filter(id -> !id.equals(controllerId)).toList();
                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (!excludeIds.contains(c.getId()) && matchesGraveyardEffectTypeFilter(effect, c, null)) {
                            validIds.add(c.getId());
                        }
                    }
                }
                break;
            }
        }
        return validIds;
    }

    /**
     * Checks whether a graveyard card matches the type restriction imposed by the given effect.
     * Mirrors the validation in {@link com.github.laxika.magicalvibes.service.validate.GraveyardTargetValidators}.
     */
    private boolean matchesGraveyardEffectTypeFilter(CardEffect effect, Card c, UUID sourceCardId) {
        if (effect instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect) {
            return c.hasType(CardType.CREATURE);
        } else if (effect instanceof CastTargetInstantOrSorceryFromGraveyardEffect) {
            return c.hasType(CardType.INSTANT) || c.hasType(CardType.SORCERY);
        } else if (effect instanceof GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect) {
            return c.hasType(CardType.CREATURE);
        } else if (effect instanceof ExileGraveyardCardsEffect e
                && e.scope() == GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD && e.filter() != null) {
            return predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof GrantFlashbackToTargetGraveyardCardEffect e) {
            return e.cardTypes().stream().anyMatch(c::hasType);
        } else if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect e && e.filter() != null) {
            return predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof ExileTargetCardFromGraveyardAndCreateTokenCopyEffect e && e.filter() != null) {
            return predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof PlayTargetCardFromGraveyardWithoutPayingManaCostEffect e && e.filter() != null) {
            return predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect e) {
            return e.filter() == null || predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof ExileTargetGraveyardCardAndSameNameFromZonesEffect) {
            return !(c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        }
        return true;
    }

    /**
     * Checks shroud, hexproof, and granted hexproof (TargetingRestrictionEffect) on a permanent.
     * Returns true if the permanent is blocked from being targeted by the given controller.
     */
    private boolean isBlockedByHexproofOrGrantedEffect(GameData gameData, Permanent perm, UUID controllerId) {
        // Shroud
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)) {
            return true;
        }

        // Hexproof (only blocks if target is opponent's)
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return true;
            }
        }

        // Granted hexproof-like effect (TargetingRestrictionEffect hexproof, e.g. Asceticism)
        if (gameQueryService.cantBeTargetedBySpellsOrAbilities(gameData, perm)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks "hexproof from [color]" on a permanent.
     * Returns true if the permanent has hexproof from the source's color and is controlled by an opponent.
     */
    private boolean isBlockedByHexproofFromColor(GameData gameData, Permanent perm, CardColor sourceColor, UUID controllerId) {
        if (sourceColor == null) {
            return false;
        }
        if (gameQueryService.hasHexproofFromColor(gameData, perm, sourceColor)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates a target filter against a permanent. Returns true if the filter is null or the permanent passes.
     */
    private boolean passesTargetFilter(GameData gameData, TargetFilter filter, Permanent perm, UUID sourceCardId, UUID controllerId) {
        if (filter == null) {
            return true;
        }
        try {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(sourceCardId)
                    .withSourceControllerId(controllerId);
            predicateEvaluationService.validateTargetFilter(filter, perm, filterContext);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private boolean isPlaneswalker(Permanent perm) {
        return perm.getCard().hasType(CardType.PLANESWALKER);
    }
}
