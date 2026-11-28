package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.StepKind;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

/**
 * Shared bounce helpers used by every "normal" Bounce effect handler.
 *
 * <p>Extracted verbatim from {@code BounceResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BounceSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final StateTriggerService stateTriggerService;

    public void applyReturnSelfToHand(GameData gameData, StackEntry entry) {
        applyReturnSelfToHand(gameData, entry,
                gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()));
    }

    /**
     * Returns the resolving ability's source permanent, {@code toReturn}, to its owner's hand; {@code null} when it is
     * no longer on the battlefield.
     */
    public void applyReturnSelfToHand(GameData gameData, StackEntry entry, Permanent toReturn) {
        if (toReturn == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is no longer on the battlefield."));
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, toReturn.getId());
        boolean returned = permanentRemovalService.removePermanentToHand(gameData, toReturn);
        if (returned) {
            entry.rememberRemovedPermanentController(toReturn.getId(), controllerId);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is returned to its owner's hand."));
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
    }

    public int applyReturnPermanentsToHand(GameData gameData, StackEntry entry, List<Permanent> toReturn) {
        int[] returnedControlledNontokens = {0};
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            // They leave together (CR 603.10a): each one's leave abilities look back at the battlefield as it was
            // before any of them left (an Aura that removed a creature's abilities, returned with it).
            permanentRemovalService.performSimultaneousRemovals(gameData, toReturn, () -> {
                for (Permanent permanent : toReturn) {
                    UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                    boolean controlledNontoken = entry.getControllerId().equals(controllerId)
                            && !permanent.getCard().isToken();
                    boolean returned = permanentRemovalService.removePermanentToHand(gameData, permanent);
                    if (returned) {
                        entry.rememberRemovedPermanentController(permanent.getId(), controllerId);
                    }
                    if (returned && controlledNontoken) {
                        returnedControlledNontokens[0]++;
                    }

                    gameLogService.append(gameData,
                            GameLog.cardThen(permanent.getCard(), " is returned to its owner's hand."));
                    log.info("Game {} - {} returned to owner's hand by {}",
                            gameData.id, permanent.getCard().getName(), entry.getCard().getName());
                }
            });
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        if (!toReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
        return returnedControlledNontokens[0];
    }

    /**
     * A choice's permanents return as one event (CR 603.10a) and are added to the RETURN step: only those that went to
     * a hand, not one a replacement exiled instead.
     */
    public void returnChosenTogether(GameData gameData, StackEntry entry, List<Permanent> chosen) {
        Map<UUID, LastKnownCardStats> known = new LinkedHashMap<>();
        Map<UUID, List<Card>> leaving = new LinkedHashMap<>();
        for (Permanent permanent : chosen) {
            known.put(permanent.getId(), ReturnStepSupport.lastKnown(gameData, gameQueryService, permanent));
            leaving.put(permanent.getId(), List.copyOf(permanent.cardsLeavingBattlefield()));
        }
        List<Permanent> returned = new ArrayList<>();
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            permanentRemovalService.performSimultaneousRemovals(gameData, chosen, () -> {
                for (Permanent permanent : chosen) {
                    if (permanentRemovalService.removePermanentToHand(gameData, permanent, true)) {
                        returned.add(permanent);
                        entry.rememberRemovedPermanentController(permanent.getId(),
                                known.get(permanent.getId()).controllerId());
                        gameLogService.append(gameData,
                                GameLog.cardThen(permanent.getCard(), " is returned to its owner's hand."));
                    }
                }
            });
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        for (Permanent permanent : returned) {
            entry.addToStep(StepKind.RETURN, ReturnStepSupport.returned(gameData, permanent,
                    leaving.get(permanent.getId()), known.get(permanent.getId())));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Removes a spell from the stack and returns its card to its owner's hand. Not countering —
     * uncounterable spells are still bounced; copies cease to exist. Abilities on the stack are
     * ignored (only true spells are returned).
     */
    public void returnSpellToOwnerHand(GameData gameData, StackEntry source, UUID targetCardId) {
        StackEntry target = null;
        for (StackEntry se : gameData.stack) {
            if (se.isIdentifiedBy(targetCardId)) {
                target = se;
                break;
            }
        }
        if (target == null) {
            log.info("Game {} - Spell bounce target no longer on stack", gameData.id);
            return;
        }

        StackEntryType type = target.getEntryType();
        if (type == StackEntryType.ACTIVATED_ABILITY || type == StackEntryType.TRIGGERED_ABILITY) {
            log.info("Game {} - Spell bounce ignores ability on stack", gameData.id);
            return;
        }

        gameData.stack.remove(target);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (!target.isCopy()) {
            Card spell = target.getPhysicalCard();
            UUID ownerId = spell.getOwnerId() != null ? spell.getOwnerId() : target.getControllerId();
            if (target.isCastWithFlashback()) {
                // CR 702.34a / 702.127a: a flashback or aftermath spell is exiled instead of going anywhere else
                // when it would leave the stack.
                gameData.addToExile(ownerId, spell);
                gameLogService.append(gameData, GameLog.isExiled(target.getCard()));
                log.info("Game {} - {} exiled {} instead of returning it to hand", gameData.id,
                        source.getCard().getName(), target.getCard().getName());
                return;
            }
            gameData.playerHands.computeIfAbsent(ownerId, id -> new java.util.ArrayList<>()).add(spell);
        }

        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
        log.info("Game {} - {} returned {} to owner's hand",
                gameData.id, source.getCard().getName(), target.getCard().getName());
    }
}
