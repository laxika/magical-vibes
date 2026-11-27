package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageShield;
import com.github.laxika.magicalvibes.model.SourceKeyedRedirectShield;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The sources of damage a player may choose (CR 609.7a): every permanent; every spell on the stack, by its card's id,
 * the resolving instant or sorcery included (it stays on the stack until it finishes resolving, CR 608.2n);
 * the source of every ability on the stack, the resolving one included, even after it left the battlefield; every
 * card an object on the stack refers to, in a graveyard, in exile or as it last was; and every source a prevention or
 * redirection effect still waiting to apply refers to. A source needn't be able to deal damage.
 */
@Component
@RequiredArgsConstructor
public class DamageSourceChoiceSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * The sources {@code resolvingEntry}'s controller may choose (CR 609.7a): every {@link #candidates candidate} but a
     * permanent that can't be chosen.
     */
    public List<UUID> choosable(GameData gameData, StackEntry resolvingEntry) {
        return candidates(gameData, null, resolvingEntry).stream()
                .filter(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null || !gameQueryService.cantBeChosen(gameData, permanent);
                })
                .toList();
    }

    /** The ids of the sources {@code resolvingEntry}'s controller may choose that match {@code sourceFilter}. */
    public List<UUID> candidates(GameData gameData, PermanentPredicate sourceFilter, StackEntry resolvingEntry) {
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (matches(gameData, perm, sourceFilter)) {
                validIds.add(perm.getId());
            }
        });
        // The resolving ability refers to its own source, which may already have left the battlefield
        // (sacrificed as a cost); its last known information is used (CR 113.7a).
        if (!isSpell(resolvingEntry.getEntryType()) && resolvingEntry.getSourcePermanentSnapshot() != null) {
            Permanent abilitySource = resolvingEntry.getSourcePermanentSnapshot();
            if (!validIds.contains(abilitySource.getId()) && matches(gameData, abilitySource, sourceFilter)) {
                validIds.add(abilitySource.getId());
            }
        }
        for (StackEntry stackEntry : gameData.stack) {
            List<UUID> referencedIds = new ArrayList<>(stackEntry.getTargetIds());
            if (stackEntry.getTargetId() != null) referencedIds.add(stackEntry.getTargetId());
            referencedIds.addAll(stackEntry.getTargetCardIds());
            for (UUID referencedId : referencedIds) {
                Card referencedCard = gameQueryService.findCardInGraveyardById(gameData, referencedId);
                if (referencedCard == null && gameData.findExiledCard(referencedId) != null) {
                    referencedCard = gameData.findExiledCard(referencedId).card();
                }
                if (referencedCard == null) {
                    referencedCard = stackEntry.lastKnownPermanentCard(referencedId);
                }
                if (referencedCard == null) {
                    referencedCard = findDepartedAbilitySourceCard(gameData, resolvingEntry, referencedId);
                }
                if (referencedCard == null) {
                    referencedCard = findDepartedPermanentCard(gameData, referencedId);
                }
                if (referencedCard != null && !validIds.contains(referencedId)
                        && matches(gameData, new Permanent(referencedCard), sourceFilter)) {
                    validIds.add(referencedId);
                }
            }
            boolean spell = isSpell(stackEntry.getEntryType());
            Permanent source = spell || isCardSourcedAbility(stackEntry) ? new Permanent(stackEntry.getCard())
                    : stackEntry.getSourcePermanentSnapshot();
            if (source == null) continue;
            if (matches(gameData, source, sourceFilter)) {
                UUID sourceId = spell ? stackEntry.getTargetableId()
                        : isCardSourcedAbility(stackEntry) ? stackEntry.getCard().getId() : source.getId();
                if (!validIds.contains(sourceId)) validIds.add(sourceId);
            }
        }
        for (UUID referencedId : waitingSourceIds(gameData)) {
            Card referencedCard = gameData.waitingDamageSourceCard(referencedId);
            if (referencedCard != null && !validIds.contains(referencedId)
                    && matches(gameData, new Permanent(referencedCard), sourceFilter)) {
                validIds.add(referencedId);
            }
        }
        return validIds;
    }

    /**
     * Who controls the candidate source {@code sourceId}, for "a source you control": a permanent's controller, a
     * spell's controller, the player who controls an ability source's damage; {@code null} for a card in a graveyard,
     * in exile or otherwise known only as it last was.
     */
    public UUID controllerOf(GameData gameData, UUID sourceId, StackEntry resolvingEntry) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, sourceId);
        if (permanent != null) {
            return gameQueryService.findPermanentController(gameData, sourceId);
        }
        if (!isSpell(resolvingEntry.getEntryType()) && resolvingEntry.getSourcePermanentSnapshot() != null
                && sourceId.equals(resolvingEntry.getSourcePermanentSnapshot().getId())) {
            return damageControllerOf(resolvingEntry);
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (isSpell(stackEntry.getEntryType())) {
                if (sourceId.equals(stackEntry.getTargetableId())) {
                    return stackEntry.getControllerId();
                }
            } else if (isCardSourcedAbility(stackEntry) ? sourceId.equals(stackEntry.getCard().getId())
                    : sourceId.equals(stackEntry.getSourcePermanentId()) || stackEntry.getSourcePermanentSnapshot() != null
                    && sourceId.equals(stackEntry.getSourcePermanentSnapshot().getId())) {
                return damageControllerOf(stackEntry);
            }
        }
        return null;
    }

    /** The card of the source {@code sourceId} a player chose, or {@code null} when it is no longer known. */
    public Card sourceCard(GameData gameData, UUID sourceId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, sourceId);
        if (permanent != null) {
            return permanent.getCard();
        }
        Card abilitySource = gameData.stack.stream()
                .filter(entry -> sourceId.equals(entry.getSourcePermanentId())
                        || entry.getSourcePermanentSnapshot() != null
                        && entry.getSourcePermanentSnapshot().getId().equals(sourceId))
                .map(StackEntry::getCard)
                .findFirst().orElse(null);
        if (abilitySource != null) {
            return abilitySource;
        }
        Card cardSourcedAbility = gameData.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getSourcePermanentSnapshot() == null && entry.getSourcePermanentId() == null
                        && entry.getCard() != null && sourceId.equals(entry.getCard().getId()))
                .map(StackEntry::getCard)
                .findFirst().orElse(null);
        if (cardSourcedAbility != null) {
            return cardSourcedAbility;
        }
        StackEntry parkedEntry = gameData.pendingEffectResolutionEntry;
        if (parkedEntry != null && parkedEntry.getSourcePermanentSnapshot() != null
                && sourceId.equals(parkedEntry.getSourcePermanentSnapshot().getId())) {
            return parkedEntry.getSourcePermanentSnapshot().getCard();
        }
        return gameData.stack.stream()
                .filter(entry -> isSpell(entry.getEntryType()))
                .filter(entry -> entry.getTargetableId().equals(sourceId))
                .map(StackEntry::getCard)
                .findFirst()
                .orElseGet(() -> findCardReferencedByStackEntryOrWaitingShield(gameData, sourceId));
    }

    private Card findCardReferencedByStackEntryOrWaitingShield(GameData gameData, UUID sourceId) {
        Card referenced = findCardReferencedByStackEntry(gameData, sourceId);
        if (referenced != null) {
            return referenced;
        }
        return waitingSourceIds(gameData).contains(sourceId) ? gameData.waitingDamageSourceCard(sourceId) : null;
    }

    /** The sources the prevention and redirection effects still waiting to apply refer to. */
    private static Set<UUID> waitingSourceIds(GameData gameData) {
        Set<UUID> ids = new LinkedHashSet<>();
        Stream.of(
                gameData.shields(CreatureDamageRedirectShield.class).stream().map(CreatureDamageRedirectShield::damageSourceId),
                gameData.shields(PlayerSourceNextDamageRedirectShield.class).stream()
                        .map(PlayerSourceNextDamageRedirectShield::sourcePermanentId),
                gameData.shields(PlayerSourceNextDamageShield.class).stream().map(PlayerSourceNextDamageShield::sourceId),
                gameData.shields(SourceNextDamageToAnyTargetShield.class).stream()
                        .map(SourceNextDamageToAnyTargetShield::sourceId),
                gameData.shields(SourceKeyedRedirectShield.class).stream().map(SourceKeyedRedirectShield::sourceId))
                .flatMap(s -> s)
                .filter(java.util.Objects::nonNull)
                .forEach(ids::add);
        return ids;
    }

    /**
     * A card in a graveyard or in exile that a spell or ability on the stack refers to (Reanimate's target) is still a
     * legal damage source even though it is not a permanent.
     */
    private Card findCardReferencedByStackEntry(GameData gameData, UUID sourceId) {
        boolean referenced = gameData.stack.stream().anyMatch(entry ->
                sourceId.equals(entry.getTargetId())
                        || entry.getTargetIds().contains(sourceId)
                        || entry.getTargetCardIds().contains(sourceId));
        if (!referenced) {
            return null;
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, sourceId);
        if (graveyardCard != null) {
            return graveyardCard;
        }
        var exiled = gameData.findExiledCard(sourceId);
        if (exiled != null) {
            return exiled.card();
        }
        for (StackEntry stackEntry : gameData.stack) {
            Card lastKnown = stackEntry.lastKnownPermanentCard(sourceId);
            if (lastKnown != null) {
                return lastKnown;
            }
        }
        return findDepartedPermanentCard(gameData, sourceId);
    }

    private boolean matches(GameData gameData, Permanent permanent, PermanentPredicate sourceFilter) {
        return sourceFilter == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, sourceFilter);
    }

    private static UUID damageControllerOf(StackEntry abilityEntry) {
        return abilityEntry.getDamageSourceControllerId() != null
                ? abilityEntry.getDamageSourceControllerId() : abilityEntry.getControllerId();
    }

    /** An ability whose source never was a permanent (a trigger of a discarded card) is sourced by the card. */
    private static boolean isCardSourcedAbility(StackEntry entry) {
        return !isSpell(entry.getEntryType()) && entry.getSourcePermanentSnapshot() == null
                && entry.getSourcePermanentId() == null && entry.getCard() != null;
    }

    /**
     * Last-known card of a permanent that left the battlefield while it was the source of an ability (on the stack or
     * the resolving entry), matched by the permanent's id as a stack reference holds it.
     */
    private static Card findDepartedPermanentCard(GameData gameData, UUID permanentId) {
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> snapshot.getId().equals(permanentId))
                .map(Permanent::getCard)
                .findFirst()
                .orElse(null);
    }

    private static Card findDepartedAbilitySourceCard(GameData gameData, StackEntry resolvingEntry, UUID permanentId) {
        if (matchesAbilitySourceSnapshot(resolvingEntry, permanentId)) {
            return resolvingEntry.getSourcePermanentSnapshot().getCard();
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (matchesAbilitySourceSnapshot(stackEntry, permanentId)) {
                return stackEntry.getSourcePermanentSnapshot().getCard();
            }
        }
        return null;
    }

    private static boolean matchesAbilitySourceSnapshot(StackEntry entry, UUID permanentId) {
        return !isSpell(entry.getEntryType()) && entry.getSourcePermanentSnapshot() != null
                && permanentId.equals(entry.getSourcePermanentSnapshot().getId());
    }

    private static boolean isSpell(StackEntryType type) {
        return type != StackEntryType.ACTIVATED_ABILITY && type != StackEntryType.TRIGGERED_ABILITY;
    }
}
