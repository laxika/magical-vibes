package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
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
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidTargetService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;
    private final TargetValidationService targetValidationService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Autowired
    public ValidTargetService(GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService,
                              TargetLegalityService targetLegalityService,
                              TargetValidationService targetValidationService,
                              TargetPredicateEvaluationService targetPredicateEvaluationService,
                              AmountEvaluationService amountEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.targetLegalityService = targetLegalityService;
        this.targetValidationService = targetValidationService;
        this.targetPredicateEvaluationService = targetPredicateEvaluationService;
        this.amountEvaluationService = amountEvaluationService;
    }

    public ValidTargetService(GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService,
                              TargetLegalityService targetLegalityService,
                              TargetValidationService targetValidationService,
                              TargetPredicateEvaluationService targetPredicateEvaluationService) {
        this(gameQueryService, predicateEvaluationService, targetLegalityService, targetValidationService,
                targetPredicateEvaluationService, new AmountEvaluationService(predicateEvaluationService, gameQueryService));
    }

    /**
     * Legacy 2-arg constructor for contexts that only enumerate player targets for triggers
     * (e.g. StepTriggerService's upkeep pipeline) and never route a permanent through the shared
     * spell-target core. The structural and validator collaborators are left null; calling any
     * permanent-legality method on such an instance would NPE by design.
     */
    public ValidTargetService(GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService) {
        this(gameQueryService, predicateEvaluationService, null, null, null);
    }

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds) {
        return computeValidTargetsForSpell(gameData, card, controllerId, alreadySelectedIds, null, null);
    }

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds, Integer xValue) {
        return computeValidTargetsForSpell(gameData, card, controllerId, alreadySelectedIds, xValue, null);
    }

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds, Integer xValue, Boolean kicked) {
        boolean isMultiTarget = card.getMaxTargets() > 1;
        int effectiveXValue = resolveCastTimeXValue(gameData, card, controllerId, xValue);

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

            if (!gameQueryService.isPeaceTalksActive(gameData)) {
            List<CardEffect> effectiveSpellEffects = spellEffects;
            gameData.forEachPermanent((playerId, perm) -> {
                if (excludeIds.contains(perm.getId())) return;
                if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter,
                        effectiveSpellEffects, xValue, kicked)) {
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
            // Cross-target restriction (Bioshift): later positions may only choose permanents
            // controlled by the first target's controller.
            if (card.getMultiTargetConstraint() == MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET
                    && alreadySelectedIds != null && !alreadySelectedIds.isEmpty()) {
                UUID requiredControllerId = controllerOfFirstTarget(gameData, alreadySelectedIds.getFirst());
                validPermanentIds.removeIf(id ->
                        !java.util.Objects.equals(requiredControllerId,
                                gameQueryService.findPermanentController(gameData, id)));
            }
            if (card.getMultiTargetConstraint()
                    == MultiTargetConstraint.SAME_CREATURE_OR_LAND_TYPE_AS_FIRST_AURA_HOST
                    && alreadySelectedIds != null && !alreadySelectedIds.isEmpty()) {
                Permanent aura = gameQueryService.findPermanentById(gameData, alreadySelectedIds.getFirst());
                validPermanentIds.removeIf(id -> {
                    Permanent candidate = gameQueryService.findPermanentById(gameData, id);
                    return !isAnotherPermanentOfAuraHostType(gameData, aura, candidate);
                });
            }
            if (card.getMultiTargetConstraint() == MultiTargetConstraint.ATTACHED_TO_FIRST_TARGET
                    && alreadySelectedIds != null && !alreadySelectedIds.isEmpty()
                    && !excludeIds.isEmpty()) {
                Permanent firstTarget = gameQueryService.findPermanentById(
                        gameData, alreadySelectedIds.getFirst());
                validPermanentIds.removeIf(id -> {
                    Permanent target = gameQueryService.findPermanentById(gameData, id);
                    return firstTarget == null || target == null
                            || !firstTarget.getId().equals(target.getAttachedTo());
                });
            }
            if (isOnePerControllerConstraint(card.getMultiTargetConstraint())
                    && !excludeIds.isEmpty()) {
                Set<UUID> selectedControllers = excludeIds.stream()
                        .map(id -> gameQueryService.findPermanentController(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
                validPermanentIds.removeIf(id ->
                        selectedControllers.contains(gameQueryService.findPermanentController(gameData, id)));
            }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            boolean positionAllowsPlayers;
            if (!isMultiTarget) {
                positionAllowsPlayers = true;
            } else {
                positionAllowsPlayers = card.doesPositionAllowPlayerTargets(positionIndex);
            }

            if (positionAllowsPlayers && !gameQueryService.isPeaceTalksActive(gameData)) {
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, modeFilter != null ? modeFilter : card.getTargetFilter(),
                            playerId, controllerId, null, card)) {
                        validPlayerIds.add(playerId);
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            // A graveyard target group declares its own scope + card filter, so per-position
            // enumeration honours the group being filled (Spelltwine: own graveyard, then an
            // opponent's). Groups that declare no graveyard filter keep the card-wide enumeration.
            TargetFilter graveyardPositionFilter = isMultiTarget && positionIndex < card.getMultiTargetFilters().size()
                    ? card.getMultiTargetFilters().get(positionIndex)
                    : null;
            if (graveyardPositionFilter instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                validGraveyardCardIds.addAll(
                        computeValidGraveyardTargetsForFilter(gameData, card, graveyardFilter, controllerId, excludeIds));
            } else {
                validGraveyardCardIds.addAll(computeValidGraveyardTargets(gameData, card, spellEffects, controllerId, xValue));
            }
        }

        String prompt = "Select a target for " + card.getName();
        if (isMultiTarget) {
            prompt = "Select targets for " + card.getName();
        }

        boolean isKicked = Boolean.TRUE.equals(kicked);
        int responseMinTargets = card.getEffectiveMinTargets(effectiveXValue, isKicked);
        int effectiveX = xValue != null ? xValue : 0;
        int responseMaxTargets = targetLegalityService.getEffectiveMaxTargets(
                gameData, card, controllerId, effectiveX, isKicked);
        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, validGraveyardCardIds, responseMinTargets, responseMaxTargets, prompt);
    }

    private int resolveCastTimeXValue(GameData gameData, Card card, UUID controllerId, Integer announcedXValue) {
        int announced = announcedXValue != null ? announcedXValue : 0;
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(com.github.laxika.magicalvibes.model.effect.CastTimeXValueEffect.class::isInstance)
                .map(com.github.laxika.magicalvibes.model.effect.CastTimeXValueEffect.class::cast)
                .map(com.github.laxika.magicalvibes.model.effect.CastTimeXValueEffect::castTimeXValue)
                .filter(amount -> amount != null)
                .findFirst()
                .map(amount -> amountEvaluationService.evaluate(gameData, amount,
                        AmountContext.forCasting(controllerId, announced, card)))
                .orElse(announced);
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
        if (modal == null) return null;
        if (modal.choicesRequired() == 1 && modal.choicesMax() == 1) {
            if (xValue < 0 || xValue >= modal.options().size()) return null;
            return modal.options().get(xValue);
        }
        // Variable / choose-multiple: only resolve a single chosen mode for per-mode filter preview
        if (xValue < 0) {
            try {
                List<Integer> chosen = modal.decodeModeIndices(xValue);
                if (chosen.size() == 1) {
                    return modal.options().get(chosen.getFirst());
                }
            } catch (IllegalStateException ignored) {
                return null;
            }
        }
        return null;
    }

    public ValidTargetsResponse computeValidTargetsForAbility(GameData gameData, Card sourceCard, ActivatedAbility ability, UUID controllerId, int permanentIndex) {
        return computeValidTargetsForAbility(gameData, sourceCard, ability, controllerId, permanentIndex, List.of());
    }

    public ValidTargetsResponse computeValidTargetsForAbility(GameData gameData, Card sourceCard, ActivatedAbility ability, UUID controllerId, int permanentIndex, List<UUID> alreadySelectedIds) {
        return computeValidTargetsForAbility(gameData, sourceCard, ability, controllerId, permanentIndex, alreadySelectedIds, null);
    }

    /**
     * Enumerates an ability's legal targets. {@code xValue} is the X the player announced for an
     * {@code {X}} cost and bounds the target count for X-scaled abilities (Runed Arch's
     * "X target creatures with power 2 or less").
     */
    public ValidTargetsResponse computeValidTargetsForAbility(GameData gameData, Card sourceCard, ActivatedAbility ability, UUID controllerId, int permanentIndex, List<UUID> alreadySelectedIds, Integer xValue) {
        List<UUID> validPermanentIds = new ArrayList<>();
        List<UUID> validPlayerIds = new ArrayList<>();
        List<UUID> validGraveyardCardIds = new ArrayList<>();
        Set<UUID> excludeIds = alreadySelectedIds != null && !alreadySelectedIds.isEmpty()
                && !ability.isAllowSharedTargets() ? Set.copyOf(alreadySelectedIds) : Set.of();
        // Source-relative player predicates ("dealt damage by this creature this turn") key their
        // per-turn records by permanent id, so resolve the ability's own permanent up front.
        UUID abilitySourcePermanentId = targetLegalityService.findSourcePermanentIdByCardId(gameData, sourceCard.getId());
        int effectiveTargetScalingValue = xValue != null ? xValue : 0;
        if (ability.getSourceCounterScaledTargetsType() != null && abilitySourcePermanentId != null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, abilitySourcePermanentId);
            if (sourcePermanent != null) {
                effectiveTargetScalingValue = sourcePermanent.getCounterCount(ability.getSourceCounterScaledTargetsType());
            }
        }

        // A filterless group ability ("each of up to six targets") declares no per-position filters
        // but still announces a group, so it takes the multi-target path too — mirroring the
        // dispatch in AbilityActivationService. X-scaled groups keep the single-target path, which
        // derives their bounds from the paid X.
        if (ability.isMultiTarget() || (ability.getMaxTargets() > 1 && !ability.isXScaledTargets())) {
            // Multi-target ability: use per-position filter
            int positionIndex = alreadySelectedIds != null ? alreadySelectedIds.size() : 0;
            TargetFilter positionFilter = positionIndex < ability.getMultiTargetFilters().size()
                    ? ability.getMultiTargetFilters().get(positionIndex)
                    : null;

            if (positionFilter instanceof PlayerPredicateTargetFilter) {
                // Player-targeting position: add valid players
                if (!gameQueryService.isPeaceTalksActive(gameData)) {
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, ability.getTargetFilter(), playerId, controllerId,
                            abilitySourcePermanentId, sourceCard)) {
                        validPlayerIds.add(playerId);
                    }
                }
                }
            } else if (positionFilter instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                validGraveyardCardIds.addAll(computeValidGraveyardTargetsForFilter(
                        gameData, sourceCard, graveyardFilter, controllerId, excludeIds));
            } else if (positionFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
                // "Target player or planeswalker" position (Chandra, Pyromaster +1): players
                // matching the filter's player predicate alongside permanents matching its
                // permanent predicate.
                if (!gameQueryService.isPeaceTalksActive(gameData)) {
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, anyFilter, playerId, controllerId,
                            abilitySourcePermanentId, sourceCard)) {
                        validPlayerIds.add(playerId);
                    }
                }
                }
                if (!gameQueryService.isPeaceTalksActive(gameData)) {
                gameData.forEachPermanent((playerId, perm) -> {
                    if (excludeIds.contains(perm.getId())) return;
                    if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, false, permanentIndex, positionFilter)) {
                        validPermanentIds.add(perm.getId());
                    }
                });
                }
            } else {
                // An unfiltered position is restricted by what the ability's own effects declare —
                // "each of up to N targets" (Chandra, the Firebrand −6) declares "any target"
                // (CR 115.4), so it offers players alongside creatures and planeswalkers and never
                // another permanent type.
                boolean unfiltered = positionFilter == null;
                PermanentPredicate declared = unfiltered
                        ? EffectResolution.declaredPermanentRestriction(ability.getEffects()).orElse(null)
                        : null;
                if (unfiltered && EffectResolution.allowsPlayerTargets(ability.getEffects())
                        && !gameQueryService.isPeaceTalksActive(gameData)) {
                    for (UUID playerId : gameData.playerIds) {
                        if (excludeIds.contains(playerId)) continue;
                        if (isValidPlayerTarget(gameData, ability.getTargetFilter(), playerId, controllerId,
                                abilitySourcePermanentId, sourceCard)) {
                            validPlayerIds.add(playerId);
                        }
                    }
                }
                if (!gameQueryService.isPeaceTalksActive(gameData)) {
                gameData.forEachPermanent((playerId, perm) -> {
                    if (excludeIds.contains(perm.getId())) return;
                    if (declared != null && !targetPredicateEvaluationService.matchesPermanent(
                            TargetPredicates.permanents(declared), perm,
                            targetFilterContext(gameData, sourceCard.getId(), controllerId, xValue))) {
                        return;
                    }
                    if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, false, permanentIndex, positionFilter)) {
                        validPermanentIds.add(perm.getId());
                    }
                });
                }
            }

            // Cross-target restriction (Chandra, Pyromaster +1): later positions may only choose
            // permanents controlled by the first target.
            if (ability.getMultiTargetConstraint() == MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET
                    && alreadySelectedIds != null && !alreadySelectedIds.isEmpty()) {
                UUID requiredControllerId = controllerOfFirstTarget(gameData, alreadySelectedIds.getFirst());
                validPlayerIds.clear();
                validPermanentIds.removeIf(id ->
                        !java.util.Objects.equals(requiredControllerId,
                                gameQueryService.findPermanentController(gameData, id)));
                validGraveyardCardIds.removeIf(id ->
                        !java.util.Objects.equals(requiredControllerId,
                                gameQueryService.findGraveyardOwnerById(gameData, id)));
            }
            if (ability.getMultiTargetConstraint() == MultiTargetConstraint.ATTACHED_TO_FIRST_TARGET
                    && alreadySelectedIds != null && !alreadySelectedIds.isEmpty()) {
                Permanent firstTarget = gameQueryService.findPermanentById(gameData, alreadySelectedIds.getFirst());
                validPlayerIds.clear();
                validPermanentIds.removeIf(id -> {
                    Permanent target = gameQueryService.findPermanentById(gameData, id);
                    return firstTarget == null || target == null
                            || !firstTarget.getId().equals(target.getAttachedTo());
                });
            }

            // "Up to two creatures and up to two lands" (Nissa, Genesis Mage +2): drop candidates
            // that would make a legal assignment to the two quotas impossible.
            if (ability.getMultiTargetConstraint() == MultiTargetConstraint.AT_MOST_TWO_CREATURES_AND_TWO_LANDS) {
                List<UUID> already = alreadySelectedIds != null ? alreadySelectedIds : List.of();
                validPermanentIds.removeIf(id -> {
                    List<UUID> trial = new ArrayList<>(already);
                    trial.add(id);
                    return !targetLegalityService.fitsAtMostTwoCreaturesAndTwoLands(gameData, trial);
                });
            }
            if (isOnePerControllerConstraint(ability.getMultiTargetConstraint())
                    && alreadySelectedIds != null && !alreadySelectedIds.isEmpty()) {
                Set<UUID> selectedControllers = alreadySelectedIds.stream()
                        .map(id -> gameQueryService.findPermanentController(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
                validPermanentIds.removeIf(id ->
                        selectedControllers.contains(gameQueryService.findPermanentController(gameData, id)));
            }

            String prompt = "Select targets for " + sourceCard.getName() + " ability";
            return new ValidTargetsResponse(validPermanentIds, validPlayerIds, validGraveyardCardIds,
                    ability.getEffectiveMinTargets(effectiveTargetScalingValue),
                    ability.getEffectiveMaxTargets(effectiveTargetScalingValue), prompt);
        }

        boolean targetsPlayer = ability.getEffects().stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean targetsPermanent = ability.getEffects().stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean targetsGraveyard = ability.getEffects().stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean targetsBlockingThis = ability.getEffects().stream()
                .anyMatch(e -> e instanceof DestroyCreatureBlockingThisEffect);

        if (targetsPermanent) {
            gameData.forEachPermanent((playerId, perm) -> {
                if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, targetsBlockingThis, permanentIndex, null)) {
                    validPermanentIds.add(perm.getId());
                }
            });
        }

        if (targetsPlayer && !gameQueryService.isPeaceTalksActive(gameData)) {
            for (UUID playerId : gameData.playerIds) {
                if (isValidPlayerTarget(gameData, ability.getTargetFilter(), playerId, controllerId,
                        abilitySourcePermanentId, sourceCard)) {
                    validPlayerIds.add(playerId);
                }
            }
        }

        if (targetsGraveyard) {
            validGraveyardCardIds.addAll(computeValidGraveyardTargetsForAbility(
                    gameData, ability, controllerId, excludeIds, sourceCard.getId()));
        }

        int minTargets = 1;
        int maxTargets = 1;
        String prompt = "Select a target for " + sourceCard.getName() + " ability";

        if (ability.isXScaledTargets()) {
            minTargets = ability.getEffectiveMinTargets(effectiveTargetScalingValue);
            maxTargets = ability.getEffectiveMaxTargets(effectiveTargetScalingValue);
            prompt = "Select targets for " + sourceCard.getName() + " ability";
        }

        // Multi-target graveyard ability (e.g. "exile two target cards")
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof ExileGraveyardCardsEffect graveyardEffect
                    && graveyardEffect.scope() == GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD) {
                minTargets = graveyardEffect.count();
                maxTargets = graveyardEffect.count();
                prompt = "Select " + graveyardEffect.count() + " target cards from an opponent's graveyard";
                break;
            }
            // "Exile up to N target cards from a single graveyard" (Rag Dealer): "up to" allows zero
            if (effect instanceof ExileGraveyardCardsEffect graveyardEffect
                    && graveyardEffect.scope() == GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD
                    && graveyardEffect.count() > 1) {
                minTargets = 0;
                maxTargets = graveyardEffect.count();
                prompt = "Select up to " + graveyardEffect.count() + " target cards from a single graveyard";
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
        return canPermanentBeTargetedBySpell(gameData, perm, spellCard, castingPlayerId, null);
    }

    /** As above, evaluating an X-dependent target filter at {@code xValue} ({@code null} means X = 0). */
    private boolean canPermanentBeTargetedBySpell(GameData gameData, Permanent perm, Card spellCard,
                                                  UUID castingPlayerId, Integer xValue) {
        return canPermanentBeTargetedBySpell(gameData, perm, spellCard, castingPlayerId, xValue, null);
    }

    private boolean canPermanentBeTargetedBySpell(GameData gameData, Permanent perm, Card spellCard,
                                                  UUID castingPlayerId, Integer xValue, Boolean kicked) {
        // Structural targeting rules (protection, can't-be-targeted, shroud, hexproof, hexproof-from-color)
        // are owned by the shared spell-target core; enumeration adds only the card's TargetFilter.
        if (!targetLegalityService.checkSpellPermanentTargetableReason(gameData, perm, spellCard, castingPlayerId).isEmpty()) {
            return false;
        }

        // Card's TargetFilter
        if (!passesTargetFilter(gameData, targetFilterForKickedCast(spellCard.getTargetFilter(), kicked),
                perm, spellCard.getId(), castingPlayerId, xValue)) {
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
        return isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter,
                spellEffects, null);
    }

    private boolean isValidPermanentTarget(GameData gameData, Card card, Permanent perm, UUID controllerId,
                                            boolean isMultiTarget, TargetFilter positionFilter,
                                            List<CardEffect> spellEffects, Integer xValue) {
        return isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter,
                spellEffects, xValue, null);
    }

    private boolean isValidPermanentTarget(GameData gameData, Card card, Permanent perm, UUID controllerId,
                                            boolean isMultiTarget, TargetFilter positionFilter,
                                            List<CardEffect> spellEffects, Integer xValue, Boolean kicked) {
        // For multi-target spells with per-position filters, use protection/hexproof checks
        // but skip the global targetFilter from canPermanentBeTargetedBySpell, since the
        // per-position filter below handles type restriction for each target group.
        if (positionFilter != null) {
            if (!canPermanentBeTargetedBySpellCore(gameData, perm, card, controllerId)) {
                return false;
            }
        } else {
            if (!canPermanentBeTargetedBySpell(gameData, perm, card, controllerId, xValue, kicked)) {
                return false;
            }
        }

        // Per-position filter for multi-target spells
        if (!passesTargetFilter(gameData, targetFilterForKickedCast(positionFilter, kicked),
                perm, card.getId(), controllerId, xValue)) {
            return false;
        }

        // An unfiltered slot is restricted by what the spell's effects themselves declare. "Any
        // target" (CR 115.4: a creature, player, planeswalker or battle — battles are not modelled
        // yet) is one such declaration, evaluated layer-aware through the shared predicate
        // hierarchy (CR 613.1d).
        if (card.getTargetFilter() == null && positionFilter == null) {
            PermanentPredicate declared =
                    EffectResolution.declaredPermanentRestriction(spellEffects).orElse(null);
            if (declared != null) {
                if (!targetPredicateEvaluationService.matchesPermanent(TargetPredicates.permanents(declared),
                        perm, targetFilterContext(gameData, card.getId(), controllerId, xValue))) {
                    return false;
                }
            } else if (isMultiTarget && !gameQueryService.isCreature(gameData, perm)) {
                // Legacy default for a multi-target slot no effect restricts (Karn's Temporal
                // Sundering's bare "target player" group, whose permanents no effect claims).
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
                        new TargetValidationContext(gameData, perm.getId(), null, card,
                                xValue != null ? xValue : 0)).isPresent()) {
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

    /**
     * The player a {@link MultiTargetConstraint#CONTROLLED_BY_FIRST_TARGET} group is anchored to:
     * the first target itself when it is a player, else that permanent's controller.
     */
    private UUID controllerOfFirstTarget(GameData gameData, UUID firstTargetId) {
        if (gameData.playerIds.contains(firstTargetId)) {
            return firstTargetId;
        }
        UUID permanentController = gameQueryService.findPermanentController(gameData, firstTargetId);
        return permanentController != null
                ? permanentController
                : gameQueryService.findGraveyardOwnerById(gameData, firstTargetId);
    }

    private boolean isValidPlayerTarget(GameData gameData, TargetFilter targetFilter, UUID playerId, UUID controllerId) {
        return isValidPlayerTarget(gameData, targetFilter, playerId, controllerId, null);
    }

    /**
     * Source-aware variant. {@code sourcePermanentId} is the permanent an activated ability comes
     * from, needed by source-relative player predicates ("dealt damage by this creature this turn").
     */
    private boolean isValidPlayerTarget(GameData gameData, TargetFilter targetFilter, UUID playerId, UUID controllerId,
                                        UUID sourcePermanentId) {
        return isValidPlayerTarget(gameData, targetFilter, playerId, controllerId, sourcePermanentId, null);
    }

    private boolean isValidPlayerTarget(GameData gameData, TargetFilter targetFilter, UUID playerId, UUID controllerId,
                                        UUID sourcePermanentId, Card sourceCard) {
        // Player shroud
        if (gameQueryService.playerHasShroud(gameData, playerId)) {
            return false;
        }

        // Player hexproof (only blocks opponents)
        if (!controllerId.equals(playerId)
                && gameQueryService.playerHasHexproof(gameData, playerId)
                && !gameQueryService.ignoresOpponentPlayerHexproof(gameData, controllerId)) {
            return false;
        }

        var effectiveSourceColor = gameQueryService.getEffectiveCardColor(gameData, sourceCard);
        if (!controllerId.equals(playerId) && effectiveSourceColor != null
                && gameQueryService.playerHasHexproofFromColor(gameData, playerId, effectiveSourceColor)) {
            return false;
        }

        // PlayerPredicateTargetFilter (e.g. "target opponent")
        if (targetFilter instanceof PlayerPredicateTargetFilter playerFilter
                && !targetLegalityService.matchesPlayerPredicate(
                        gameData, controllerId, playerId, playerFilter.predicate(), sourcePermanentId)) {
            return false;
        }

        // Any-target restriction: the player side is checked against the player predicate.
        if (targetFilter instanceof AnyTargetPredicateTargetFilter anyFilter
                && !targetLegalityService.matchesPlayerPredicate(
                        gameData, controllerId, playerId, anyFilter.playerPredicate(), sourcePermanentId)) {
            return false;
        }

        return true;
    }

    public boolean isValidAbilityPermanentTargetForPosition(GameData gameData, Card sourceCard,
                                                             ActivatedAbility ability, Permanent perm,
                                                             UUID controllerId, int sourcePermanentIndex,
                                                             TargetFilter positionFilter) {
        return isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId,
                false, sourcePermanentIndex, positionFilter);
    }

    private boolean isValidAbilityPermanentTarget(GameData gameData, Card sourceCard, ActivatedAbility ability,
                                                   Permanent perm, UUID controllerId,
                                                   boolean targetsBlockingThis, int sourcePermanentIndex,
                                                   TargetFilter positionFilter) {
        if (gameQueryService.isPeaceTalksActive(gameData)) {
            return false;
        }
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
        Set<CardColor> effectiveSourceColors = gameQueryService.getEffectiveCardColors(gameData, sourceCard);
        if (effectiveSourceColors.stream()
                .anyMatch(color -> isBlockedByHexproofFromColor(gameData, perm, color, controllerId))) {
            return false;
        }

        // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, perm, sourceCard)) {
            return false;
        }

        if (gameQueryService.cantBeTargetedByWallOnlySources(gameData, perm)
                && targetLegalityService.sourceCanTargetOnlyWalls(
                sourceCard, ability.getEffects(), ability.getTargetFilter(), ability.getMultiTargetFilters())) {
            return false;
        }

        // TargetFilter from ability
        if (!passesTargetFilter(gameData, ability.getTargetFilter(), perm, sourceCard.getId(), controllerId)) {
            return false;
        }

        // Protection from source color/type/subtype (for abilities that deal damage or destroy)
        boolean dealsDamageOrDestroys = ability.getEffects().stream().anyMatch(e ->
                e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) && e.targetSpec().harmful());
        if (dealsDamageOrDestroys) {
            if (gameQueryService.hasProtectionFromOpponents(gameData, perm, controllerId)) {
                return false;
            }
            if (effectiveSourceColors.stream()
                    .anyMatch(color -> gameQueryService.hasProtectionFrom(gameData, perm, color))) {
                return false;
            }
            if (gameQueryService.hasProtectionFromSourceCardTypes(gameData, perm, sourceCard)) {
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

        // Per-effect declarative TargetSpec + @ValidatesTarget checks — the same validation
        // activation runs (TargetLegalityService.validateActivatedAbilityTargeting). Running them
        // here keeps UI/AI enumeration from offering a permanent that activation would reject:
        // without it an "any target" ability (Rod of Ruin) enumerated every permanent, so the
        // frontend highlighted artifacts and lands and the MCTS simulator searched an illegal
        // activation. Scoped to the single-target case, mirroring the spell path; multi-target
        // ability positions are governed by their per-position TargetFilter.
        if (!ability.isMultiTarget()
                && targetValidationService.checkEffectTargets(ability.getEffects(),
                        new TargetValidationContext(gameData, perm.getId(), null, sourceCard)).isPresent()) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if at least one legal target exists for the given spell.
     * Per MTG rule 601.2c, a spell can't be cast unless a legal set of targets can be chosen for it.
     */
    public boolean hasValidTargetsForSpell(GameData gameData, Card card, UUID controllerId) {
        return hasValidTargetsForSpell(gameData, card, controllerId, null);
    }

    /**
     * As above for an {@code {X}} spell whose caster could announce X up to {@code maxXValue}. X is
     * announced in CR 601.2b, before targets are chosen in CR 601.2c, so a castability pre-check has
     * to ask whether any announceable X leaves a legal target — asking only at X = 0 would report
     * Killing Glare as unplayable whenever every creature has power 1 or more.
     */
    public boolean hasValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, Integer maxXValue) {
        return hasValidTargetsForSpell(gameData, card, controllerId, maxXValue, null);
    }

    /**
     * As above, using a known kicker choice when the spell's target shape depends on it.
     */
    public boolean hasValidTargetsForSpell(GameData gameData, Card card, UUID controllerId,
                                           Integer maxXValue, Boolean kicked) {
        List<CardEffect> spellEffects = kicked == null
                ? card.getEffects(EffectSlot.SPELL)
                : EffectResolution.resolveEffects(card.getEffects(EffectSlot.SPELL), kicked, null);
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(
                spellEffects, card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD),
                card.isAura(), card.isEnchantPlayer());
        boolean isMultiTarget = card.getMaxTargets() > 1;

        if (allowedTargets.contains(TargetType.PERMANENT)
                && anyAnnounceableXHasPermanentTarget(gameData, card, controllerId, isMultiTarget, maxXValue,
                spellEffects, kicked)) {
            return true;
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
                    if (isValidPlayerTarget(gameData, card.getTargetFilter(), playerId, controllerId, null, card)) {
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
     * Whether some X the caster could announce leaves a legal permanent target for the spell. The
     * caster picks the X that makes their targets legal, so an equality-based filter — Entrancing
     * Melody's "creature with mana value X" — is satisfiable by a 1-drop as long as X = 1 is
     * affordable, however much more mana is available. {@code maxXValue} is null for spells with no
     * {@code {X}}, which are checked once at the filter's default X = 0.
     */
    private boolean anyAnnounceableXHasPermanentTarget(GameData gameData, Card card, UUID controllerId,
                                                       boolean isMultiTarget, Integer maxXValue,
                                                       List<CardEffect> spellEffects, Boolean kicked) {
        for (int x = maxXValue == null ? 0 : maxXValue; x >= 0; x--) {
            Integer xValue = maxXValue == null ? null : x;
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent perm : battlefield) {
                    if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, null,
                            spellEffects, xValue, kicked)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Computes valid graveyard card targets for a spell. Handles scope filtering (controller's
     * graveyard, opponent's, or all), card predicate filtering, and X-value mana value matching.
     */
    /**
     * Enumerates the legal cards for a graveyard target group that declares its own scope and card
     * filter, excluding cards already chosen for an earlier group.
     */
    public List<UUID> computeValidGraveyardTargetsForFilter(GameData gameData, Card card,
                                                            GraveyardCardPredicateTargetFilter filter,
                                                            UUID controllerId, Set<UUID> excludeIds) {
        if (!gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            return List.of();
        }
        List<UUID> searchPlayerIds = filter.scope().graveyardOwners(gameData.orderedPlayerIds, controllerId);

        List<UUID> validIds = new ArrayList<>();
        for (UUID playerId : searchPlayerIds) {
            if (isOnePerControllerConstraint(card.getMultiTargetConstraint())
                    && !excludeIds.isEmpty()) {
                Set<UUID> selectedControllers = excludeIds.stream()
                        .map(id -> {
                            UUID permanentController = gameQueryService.findPermanentController(gameData, id);
                            return permanentController != null
                                    ? permanentController
                                    : gameQueryService.findGraveyardOwnerById(gameData, id);
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
                if (selectedControllers.contains(playerId)) {
                    continue;
                }
            }
            for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                if (excludeIds.contains(c.getId())) continue;
                if (filter.predicate() != null
                        && !predicateEvaluationService.matchesCardPredicate(c, filter.predicate(), card.getId())) {
                    continue;
                }
                validIds.add(c.getId());
            }
        }
        return validIds;
    }

    private boolean isOnePerControllerConstraint(MultiTargetConstraint constraint) {
        return constraint == MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER
                || constraint == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE;
    }

    private List<UUID> computeValidGraveyardTargets(GameData gameData, Card card, List<CardEffect> spellEffects, UUID controllerId, Integer xValue) {
        if (!gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            return List.of();
        }
        int effectiveXValue = xValue != null ? xValue : 0;
        List<UUID> validIds = new ArrayList<>();

        for (CardEffect effect : spellEffects) {
            GraveyardSearchScope scope = effect.targetSpec().graveyardScope().orElse(null);
            if (scope == null) continue;

            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                List<UUID> searchPlayerIds = scope.graveyardOwners(gameData.orderedPlayerIds, controllerId);

                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (!matchesReturnCardFilter(gameData, rge, c, card.getId())) {
                            continue;
                        }
                        if (rge.requiresManaValueEqualsX() && c.getManaValue() != effectiveXValue) {
                            continue;
                        }
                        if (rge.requiresManaValueAtMostX() && c.getManaValue() > effectiveXValue) {
                            continue;
                        }
                        if (rge.targetPutIntoGraveyardFromBattlefieldThisTurn()
                                && !gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn
                                        .getOrDefault(playerId, Set.of()).contains(c.getId())) {
                            continue;
                        }
                        validIds.add(c.getId());
                    }
                }
            } else {
                // Generic graveyard-targeting effects (e.g. PutCreatureFromOpponentGraveyard)
                List<UUID> searchPlayerIds = scope.graveyardOwners(gameData.orderedPlayerIds, controllerId);

                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (!matchesGraveyardEffectTypeFilter(gameData, effect, c, card.getId())) continue;
                        validIds.add(c.getId());
                    }
                }
            }

            if (!validIds.isEmpty()) break;
        }

        return validIds;
    }

    private List<UUID> computeValidGraveyardTargetsForAbility(GameData gameData, ActivatedAbility ability,
                                                                UUID controllerId, Set<UUID> excludeIds,
                                                                UUID sourceCardId) {
        if (!gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            return List.of();
        }
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
            GraveyardSearchScope scope = effect.targetSpec().graveyardScope().orElse(null);
            if (scope != null) {
                List<UUID> searchPlayerIds = scope.graveyardOwners(gameData.orderedPlayerIds, controllerId);
                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (!excludeIds.contains(c.getId())
                                && matchesGraveyardEffectTypeFilter(gameData, effect, c, sourceCardId)) {
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
    private boolean matchesGraveyardEffectTypeFilter(GameData gameData, CardEffect effect, Card c, UUID sourceCardId) {
        if (effect instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect) {
            return c.hasType(CardType.CREATURE);
        } else if (effect instanceof CastTargetInstantOrSorceryFromGraveyardEffect) {
            return c.hasType(CardType.INSTANT) || c.hasType(CardType.SORCERY);
        } else if (effect instanceof GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect) {
            return c.hasType(CardType.CREATURE);
        } else if (effect instanceof GrantTargetGraveyardCardCastEffect e) {
            return e.filter() == null || predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof ExileGraveyardCardsEffect e
                && e.scope() == GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD && e.filter() != null) {
            return predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof GrantFlashbackToTargetGraveyardCardEffect e) {
            return e.cardTypes().stream().anyMatch(c::hasType);
        } else if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect e && e.filter() != null) {
            return predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof ExileTargetCardFromGraveyardAndCreateTokenCopyEffect e && e.filter() != null) {
            if (!predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId)) {
                return false;
            }
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, c.getId());
            return !e.targetPutIntoGraveyardFromAnywhereThisTurn()
                    || (graveyardOwnerId != null
                    && gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                            .getOrDefault(graveyardOwnerId, Set.of()).contains(c.getId()));
        } else if (effect instanceof ExileTargetCardFromGraveyardAndCreateTokenCopyEffect e) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, c.getId());
            return !e.targetPutIntoGraveyardFromAnywhereThisTurn()
                    || (graveyardOwnerId != null
                    && gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                            .getOrDefault(graveyardOwnerId, Set.of()).contains(c.getId()));
        } else if (effect instanceof PlayTargetCardFromGraveyardWithoutPayingManaCostEffect e && e.filter() != null) {
            return predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect e) {
            return e.filter() == null || predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof ReturnCardFromGraveyardEffect e) {
            return matchesReturnCardFilter(gameData, e, c, sourceCardId);
        } else if (effect instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect e) {
            return e.filter() == null || predicateEvaluationService.matchesCardPredicate(c, e.filter(), sourceCardId);
        } else if (effect instanceof ExileTargetGraveyardCardAndSameNameFromZonesEffect) {
            return !(c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        }
        return true;
    }

    private boolean matchesReturnCardFilter(GameData gameData, ReturnCardFromGraveyardEffect effect,
                                             Card card, UUID sourceCardId) {
        if (effect.sourceChosenSubtype()) {
            CardSubtype chosenSubtype = findSourceChosenSubtype(gameData, sourceCardId);
            UUID cardOwnerId = card.getOwnerId() != null
                    ? card.getOwnerId()
                    : gameQueryService.findGraveyardOwnerById(gameData, card.getId());
            return chosenSubtype != null
                    && (card.getKeywords().contains(Keyword.CHANGELING)
                    || gameQueryService.cardHasSubtype(card, chosenSubtype, gameData, cardOwnerId));
        }
        return effect.filter() == null
                || predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId);
    }

    private CardSubtype findSourceChosenSubtype(GameData gameData, UUID sourceCardId) {
        if (sourceCardId == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(sourceCardId)
                        || permanent.getOriginalCard().getId().equals(sourceCardId)) {
                    return permanent.getChosenSubtype();
                }
            }
        }
        return null;
    }

    /**
     * Checks shroud, hexproof, and granted hexproof (TargetingRestrictionEffect) on a permanent.
     * Returns true if the permanent is blocked from being targeted by the given controller.
     */
    private boolean isBlockedByHexproofOrGrantedEffect(GameData gameData, Permanent perm, UUID controllerId) {
        // Shroud (Autumn Willow can hand out a per-player exemption for the turn)
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)
                && !perm.ignoresShroudFor(controllerId)) {
            return true;
        }

        // Glaring Spotlight: opponents' hexproof creatures are targetable as though they had none.
        boolean hexproofLifted = gameQueryService.isCreature(gameData, perm)
                && gameQueryService.ignoresOpponentCreatureHexproof(gameData, controllerId);

        // Hexproof (only blocks if target is opponent's)
        if (!hexproofLifted && gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return true;
            }
        }

        // Granted hexproof-like effect (TargetingRestrictionEffect hexproof, e.g. Asceticism)
        if (gameQueryService.cantBeTargetedByOpponentSpellsOrAbilities(gameData, perm, controllerId)) {
            return true;
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
        if (gameQueryService.cantBeTargetedByColorSources(gameData, perm, sourceColor)) {
            return true;
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
        return passesTargetFilter(gameData, filter, perm, sourceCardId, controllerId, null);
    }

    /**
     * As above, evaluating an X-dependent filter (Killing Glare's "creature with power X or less") at
     * {@code xValue}. {@code null} leaves the context's default of X = 0.
     */
    private boolean passesTargetFilter(GameData gameData, TargetFilter filter, Permanent perm, UUID sourceCardId,
                                       UUID controllerId, Integer xValue) {
        if (filter == null) {
            return true;
        }
        try {
            predicateEvaluationService.validateTargetFilter(filter, perm,
                    targetFilterContext(gameData, sourceCardId, controllerId, xValue));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private TargetFilter targetFilterForKickedCast(TargetFilter targetFilter, Boolean kicked) {
        if (targetFilter instanceof PermanentPredicateTargetFilter filter) {
            return new PermanentPredicateTargetFilter(filter.predicateFor(Boolean.TRUE.equals(kicked)), filter.errorMessage());
        }
        return targetFilter;
    }

    /**
     * The context a targeting predicate is evaluated in: the game state plus the source card and
     * its controller, which source-relative predicates ("another creature you control") need, and
     * the announced X for X-dependent ones ({@code null} leaves the default of X = 0).
     */
    private static FilterContext targetFilterContext(GameData gameData, UUID sourceCardId,
                                                     UUID controllerId, Integer xValue) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(controllerId);
        return xValue != null ? filterContext.withXValue(xValue) : filterContext;
    }
}
