package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Unified handler for {@link ReturnToHandEffect}. Dispatches on the effect's
 * {@link com.github.laxika.magicalvibes.model.effect.BounceScope}: {@code TARGET} (the chosen target
 * permanent(s), with the optional controller-life-loss rider), {@code SELF} (the source),
 * {@code SELF_SPELL} (the resolving spell returns itself to hand off the stack),
 * {@code ALL_MATCHING} (every permanent matching the filter across all battlefields),
 * {@code TARGET_PLAYERS_PERMANENTS} (the target player's matching permanents),
 * {@code TARGET_PLAYERS_OWNED} (permanents the target player owns, any controller),
 * {@code TARGET_CHOSEN_CREATURE_TYPE} (the chosen target creatures matching the cast-time type),
 * {@code AURAS_ATTACHED_TO_TARGET}, {@code ENCHANTED} (the permanent the source Aura is on), and
 * {@code ENCHANTED_AND_AURAS} (that permanent and all its attached Auras).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BounceSupport bounceSupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnToHandEffect) effect;
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry, e);
            case TARGET_NONLAND_PERMANENT_OR_SUSPENDED_CARD ->
                    resolveTargetNonlandPermanentOrSuspendedCard(gameData, entry, e);
            case TARGET_CHOSEN_CREATURE_TYPE -> resolveTargetChosenCreatureType(gameData, entry, e);
            case SELF -> bounceSupport.applyReturnSelfToHand(gameData, entry);
            case TRIGGERING -> resolveTriggering(gameData, entry, e);
            case SELF_SPELL -> resolveSelfSpell(gameData, entry);
            case ALL_MATCHING -> resolveAllMatching(gameData, entry, e);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e);
            case TARGET_PLAYERS_OWNED -> resolveTargetPlayersOwned(gameData, entry, e);
            case AURAS_ATTACHED_TO_TARGET -> resolveAurasAttachedToTarget(gameData, entry);
            case ENCHANTED -> resolveEnchanted(gameData, entry, e);
            case ENCHANTED_AND_AURAS -> resolveEnchantedAndAuras(gameData, entry, e);
            case GRANTING_EQUIPMENT -> resolveGrantingEquipment(gameData, entry, e);
        }
    }

    /**
     * The resolving spell returns itself to its owner's hand instead of going to the graveyard.
     * Normally this is just a marker read once the entry finishes resolving, but an effect that
     * paused for player input earlier in the same resolution (Petals of Insight's "you may"
     * prompt) has already had its spell disposition applied, so the card is fetched back out of
     * the graveyard here.
     */
    private void resolveSelfSpell(GameData gameData, StackEntry entry) {
        entry.setReturnToHandAfterResolving(true);
        if (entry.getCard() == null) {
            return;
        }
        UUID ownerId = entry.getOwnerId();
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard != null && graveyard.remove(entry.getPhysicalCard())) {
            gameData.addCardToHand(ownerId, entry.getPhysicalCard());
        }
    }

    private void resolveEnchanted(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        UUID hostId = e.enchantedPermanentId();
        if (hostId == null) {
            Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            // The Aura may have been sacrificed as a cost; the activation path then captures the
            // attached creature in the entry target.
            hostId = aura != null && aura.isAttached() ? aura.getAttachedTo() : entry.getTargetId();
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, hostId);
        if (enchanted == null) {
            return;
        }
        bounceAll(gameData, entry, List.of(enchanted));
    }

    private void resolveEnchantedAndAuras(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        UUID hostId = e.enchantedPermanentId();
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (hostId == null) {
            hostId = aura != null && aura.isAttached()
                    ? aura.getAttachedTo()
                    : entry.getSourcePermanentSnapshot() == null
                            ? null
                            : entry.getSourcePermanentSnapshot().getAttachedTo();
        }
        Permanent enchanted = hostId == null ? null : gameQueryService.findPermanentById(gameData, hostId);
        if (enchanted == null) {
            return;
        }

        List<Permanent> toReturn = new ArrayList<>();
        UUID finalHostId = hostId;
        gameData.forEachBattlefield((playerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(p -> p.getCard().isAura() && p.isAttached()
                                && finalHostId.equals(p.getAttachedTo()))
                        .toList()));
        toReturn.add(enchanted);
        bounceAll(gameData, entry, toReturn);
    }

    private void resolveTriggering(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        UUID triggeringPermanentId = entry.getTriggeringPermanentId();
        if (triggeringPermanentId == null) {
            return;
        }
        bounceTarget(gameData, entry, e, triggeringPermanentId);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void resolveGrantingEquipment(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        if (e.grantingEquipmentId() == null) {
            return;
        }
        Permanent equipment = gameQueryService.findPermanentById(gameData, e.grantingEquipmentId());
        if (equipment != null) {
            bounceAll(gameData, entry, List.of(equipment));
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        // Multi-target: bounce each valid target of this effect's target group — the group's slice
        // of the flat target list for effects bound via target(...).addEffect(...) (e.g. Leave:
        // "any number of target permanents you own"), or the whole flat list for unbound effects.
        // An empty group (optional / any-number targets omitted) bounces nothing.
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (!targetIds.isEmpty()) {
            for (UUID targetId : targetIds) {
                bounceTarget(gameData, entry, e, targetId);
            }
        } else if (entry.getTargetId() != null) {
            // Single-target fallback (targetId)
            bounceTarget(gameData, entry, e, entry.getTargetId());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (e.lifeLoss() > 0) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private void resolveTargetNonlandPermanentOrSuspendedCard(
            GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        if (entry.getTargetZone() != Zone.EXILE) {
            resolveTarget(gameData, entry, e);
            return;
        }

        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        var exiled = gameData.findExiledCard(targetId);
        Integer timeCounters = gameData.exiledCardTimeCounters.get(targetId);
        if (exiled == null || exiled.faceDown() || timeCounters == null || timeCounters <= 0) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target card is no longer suspended)."));
            return;
        }

        gameData.removeFromExile(targetId);
        gameData.addCardToHand(exiled.ownerId(), exiled.card());
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getDescription() + " returns ", exiled.card(), " from exile to its owner's hand."));
    }

    private void resolveTargetChosenCreatureType(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        if (entry.getChosenCreatureType() == null) {
            return;
        }

        PermanentPredicate chosenTypeFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(entry.getChosenCreatureType())));
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withXValue(entry.getXValue());
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (!targetIds.isEmpty()) {
            for (UUID targetId : targetIds) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target != null && predicateEvaluationService.matchesPermanentPredicate(
                        target, chosenTypeFilter, filterContext)) {
                    bounceTarget(gameData, entry, e, targetId);
                }
            }
        } else if (entry.getTargetId() != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target != null && predicateEvaluationService.matchesPermanentPredicate(
                    target, chosenTypeFilter, filterContext)) {
                bounceTarget(gameData, entry, e, entry.getTargetId());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void bounceTarget(GameData gameData, StackEntry entry, ReturnToHandEffect e, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID controllerId = (e.lifeLoss() > 0 || e.drawCount() > 0)
                ? gameQueryService.findPermanentController(gameData, target.getId())
                : null;

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        if (controllerId != null && e.drawCount() > 0) {
            playerInteractionSupport.applyDrawCards(gameData, controllerId, e.drawCount());
        }

        if (controllerId != null && e.lifeLoss() > 0) {
            if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId) + "'s life total can't change."));
            } else {
                int lifeLoss = e.lifeLoss()
                        * gameQueryService.opponentLifeLossMultiplier(gameData, controllerId);
                int currentLife = gameData.getLife(controllerId);
                gameData.playerLifeTotals.put(controllerId, currentLife - lifeLoss);

                String playerName = gameData.playerIdToName.get(controllerId);
                gameLogService.append(gameData, GameLog.textCardText(playerName + " loses " + lifeLoss + " life (" , entry.getCard(), ")."));
                log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, lifeLoss, entry.getCard().getName());
            }
        }
    }

    private void resolveAllMatching(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withXValue(entry.getXValue());

        FilterContext context = filterContext;
        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(p -> e.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), context))
                        .toList()));

        int returnedControlledNontokens = bounceAll(gameData, entry, toReturn);
        if (e.thenEffect() != null && returnedControlledNontokens >= e.minimumControlledNontokenCount()) {
            int effectIndex = entry.getEffectsToResolve().indexOf(e);
            if (effectIndex >= 0) {
                entry.insertEffectsToResolve(effectIndex + 1, List.of(e.thenEffect()));
            }
        }
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toReturn = battlefield.stream()
                .filter(p -> e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();

        bounceAll(gameData, entry, toReturn);
    }

    private void resolveTargetPlayersOwned(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((controllingPlayerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(p -> {
                            UUID ownerId = gameData.stolenCreatures.getOrDefault(p.getId(), controllingPlayerId);
                            return ownerId.equals(targetPlayerId);
                        })
                        .filter(p -> e.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                        .toList()));

        bounceAll(gameData, entry, toReturn);
    }

    private void resolveAurasAttachedToTarget(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null || gameQueryService.findPermanentById(gameData, targetId) == null) {
            return;
        }

        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(p -> p.getCard().isAura() && p.isAttached() && targetId.equals(p.getAttachedTo()))
                        .toList()));

        bounceAll(gameData, entry, toReturn);
    }

    private int bounceAll(GameData gameData, StackEntry entry, List<Permanent> toReturn) {
        int returnedControlledNontokens = 0;
        for (Permanent permanent : toReturn) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            boolean controlledNontoken = entry.getControllerId().equals(controllerId)
                    && !permanent.getCard().isToken();
            boolean returned = permanentRemovalService.removePermanentToHand(gameData, permanent);
            if (returned && controlledNontoken) {
                returnedControlledNontokens++;
            }

            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }

        if (!toReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
        return returnedControlledNontokens;
    }
}
