package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardsFromOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidTargetService {

    private final GameQueryService gameQueryService;

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds) {
        return computeValidTargetsForSpell(gameData, card, controllerId, alreadySelectedIds, null);
    }

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds, Integer xValue) {
        boolean isMultiTarget = card.getMaxTargets() > 1;
        Set<TargetType> allowedTargets = card.getAllowedTargets();

        List<UUID> validPermanentIds = new ArrayList<>();
        List<UUID> validPlayerIds = new ArrayList<>();
        List<UUID> validGraveyardCardIds = new ArrayList<>();
        Set<UUID> excludeIds = alreadySelectedIds != null ? Set.copyOf(alreadySelectedIds) : Set.of();

        int positionIndex = alreadySelectedIds != null ? alreadySelectedIds.size() : 0;

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            // Determine per-position filter for multi-target spells
            TargetFilter positionFilter = isMultiTarget && positionIndex < card.getMultiTargetFilters().size()
                    ? card.getMultiTargetFilters().get(positionIndex)
                    : null;

            gameData.forEachPermanent((playerId, perm) -> {
                if (excludeIds.contains(perm.getId())) return;
                if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter)) {
                    validPermanentIds.add(perm.getId());
                }
            });
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
                    if (isValidPlayerTarget(gameData, card.getTargetFilter(), playerId, controllerId)) {
                        validPlayerIds.add(playerId);
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            validGraveyardCardIds.addAll(computeValidGraveyardTargets(gameData, card, controllerId, xValue));
        }

        String prompt = "Select a target for " + card.getName();
        if (isMultiTarget) {
            prompt = "Select targets for " + card.getName();
        }

        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, validGraveyardCardIds, card.getMinTargets(), card.getMaxTargets(), prompt);
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

        boolean targetsPlayer = ability.getEffects().stream().anyMatch(CardEffect::canTargetPlayer);
        boolean targetsPermanent = ability.getEffects().stream().anyMatch(CardEffect::canTargetPermanent);
        boolean targetsGraveyard = ability.getEffects().stream().anyMatch(CardEffect::canTargetGraveyard);
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
            if (effect instanceof ExileTargetCardsFromOpponentGraveyardEffect graveyardEffect) {
                minTargets = graveyardEffect.count();
                maxTargets = graveyardEffect.count();
                prompt = "Select " + graveyardEffect.count() + " target cards from an opponent's graveyard";
                break;
            }
        }

        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, validGraveyardCardIds, minTargets, maxTargets, prompt);
    }

    /**
     * Checks whether a permanent can legally be targeted by a spell cast by the given controller.
     * Evaluates shroud, hexproof, CantBeTargetOfSpellsOrAbilities, protection from color,
     * protection from card types, cant-be-targeted-by-spell-color, and the spell's TargetFilter.
     */
    public boolean canPermanentBeTargetedBySpell(GameData gameData, Permanent perm, Card spellCard, UUID castingPlayerId) {
        CardColor sourceColor = spellCard.getColor();

        // Protection from source color
        if (gameQueryService.hasProtectionFrom(gameData, perm, sourceColor)) {
            return false;
        }
        // Protection from source card type
        if (gameQueryService.hasProtectionFromSourceCardTypes(perm, spellCard)) {
            return false;
        }
        // Protection from source subtype
        if (gameQueryService.hasProtectionFromSourceSubtypes(perm, spellCard)) {
            return false;
        }

        if (isBlockedByHexproofOrGrantedEffect(gameData, perm, castingPlayerId)) {
            return false;
        }

        // Hexproof from color (blocks opponent's spells of the specified color)
        if (isBlockedByHexproofFromColor(gameData, perm, sourceColor, castingPlayerId)) {
            return false;
        }

        // Can't be targeted by spell color
        if (gameQueryService.cantBeTargetedBySpellColor(gameData, perm, sourceColor)) {
            return false;
        }

        // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, perm, spellCard)) {
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
            List<CardEffect> permanentEffects = card.getEffects(EffectSlot.SPELL).stream()
                    .filter(CardEffect::canTargetPermanent)
                    .toList();
            boolean allAnyTarget = !permanentEffects.isEmpty()
                    && permanentEffects.stream().allMatch(CardEffect::canTargetPlayer);
            if (allAnyTarget) {
                if (!gameQueryService.isCreature(gameData, perm) && !isPlaneswalker(perm)) {
                    return false;
                }
            } else if (isMultiTarget && !gameQueryService.isCreature(gameData, perm)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Core targeting checks (protection, hexproof, shroud) without the global TargetFilter check.
     * Used by multi-target spells where per-position filters handle type restriction.
     */
    private boolean canPermanentBeTargetedBySpellCore(GameData gameData, Permanent perm, Card spellCard, UUID castingPlayerId) {
        CardColor sourceColor = spellCard.getColor();
        if (gameQueryService.hasProtectionFrom(gameData, perm, sourceColor)) return false;
        if (gameQueryService.hasProtectionFromSourceCardTypes(perm, spellCard)) return false;
        if (gameQueryService.hasProtectionFromSourceSubtypes(perm, spellCard)) return false;
        if (isBlockedByHexproofOrGrantedEffect(gameData, perm, castingPlayerId)) return false;
        if (isBlockedByHexproofFromColor(gameData, perm, sourceColor, castingPlayerId)) return false;
        if (gameQueryService.cantBeTargetedBySpellColor(gameData, perm, sourceColor)) return false;
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, perm, spellCard)) return false;
        return true;
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
        if (targetFilter instanceof PlayerPredicateTargetFilter playerFilter) {
            if (playerFilter.predicate() instanceof PlayerRelationPredicate rel) {
                if (rel.relation() == PlayerRelation.OPPONENT && controllerId.equals(playerId)) {
                    return false;
                }
                if (rel.relation() == PlayerRelation.SELF && !controllerId.equals(playerId)) {
                    return false;
                }
            }
        }

        return true;
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
                e.canTargetPermanent() && e.isDamageOrDestruction());
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
        Set<TargetType> allowedTargets = card.getAllowedTargets();
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
    private List<UUID> computeValidGraveyardTargets(GameData gameData, Card card, UUID controllerId, Integer xValue) {
        int effectiveXValue = xValue != null ? xValue : 0;
        List<UUID> validIds = new ArrayList<>();

        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!effect.canTargetGraveyard()) continue;

            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                List<UUID> searchPlayerIds = switch (rge.source()) {
                    case CONTROLLERS_GRAVEYARD -> List.of(controllerId);
                    case OPPONENT_GRAVEYARD -> gameData.orderedPlayerIds.stream()
                            .filter(id -> !id.equals(controllerId)).toList();
                    case ALL_GRAVEYARDS -> gameData.orderedPlayerIds;
                };

                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (rge.filter() != null && !gameQueryService.matchesCardPredicate(c, rge.filter(), card.getId())) {
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
                boolean anyGraveyard = effect.canTargetAnyGraveyard();
                List<UUID> searchPlayerIds = anyGraveyard
                        ? gameData.orderedPlayerIds
                        : gameData.orderedPlayerIds.stream().filter(id -> !id.equals(controllerId)).toList();

                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
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
            if (effect instanceof ExileTargetCardsFromOpponentGraveyardEffect) {
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
            if (effect.canTargetGraveyard()) {
                boolean anyGraveyard = effect.canTargetAnyGraveyard();
                List<UUID> searchPlayerIds = anyGraveyard
                        ? gameData.orderedPlayerIds
                        : gameData.orderedPlayerIds.stream().filter(id -> !id.equals(controllerId)).toList();
                for (UUID playerId : searchPlayerIds) {
                    for (Card c : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
                        if (!excludeIds.contains(c.getId())) {
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
     * Checks shroud, hexproof, and CantBeTargetOfSpellsOrAbilitiesEffect on a permanent.
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

        // CantBeTargetOfSpellsOrAbilitiesEffect (granted hexproof-like)
        if (gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
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
            gameQueryService.validateTargetFilter(filter, perm, filterContext);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private boolean isPlaneswalker(Permanent perm) {
        return perm.getCard().hasType(CardType.PLANESWALKER);
    }
}
