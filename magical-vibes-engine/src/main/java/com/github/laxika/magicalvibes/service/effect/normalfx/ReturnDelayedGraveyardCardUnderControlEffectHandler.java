package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDelayedGraveyardCardUnderControlEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the delayed "put that card onto the battlefield under [a player's] control at the
 * beginning of the next end step" trigger (Seraph, Grave Betrayal, Endless Whispers, Shirei). The
 * trigger uses the stack when the end step begins, so this runs only when it resolves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnDelayedGraveyardCardUnderControlEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDelayedGraveyardCardUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DelayedGraveyardToBattlefieldUnderControl pending =
                ((ReturnDelayedGraveyardCardUnderControlEffect) effect).pending();
        UUID ownerId = null;
        Card cardToReturn = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.getId().equals(pending.cardId())) {
                    cardToReturn = card;
                    ownerId = pid;
                    break;
                }
            }
            if (cardToReturn != null) break;
        }
        if (cardToReturn == null) {
            // No longer in a graveyard (moved/exiled/reanimated already, or it was a token) —
            // you don't get it back.
            log.info("Game {} - Delayed return under control for card {} skipped (no longer in a graveyard)",
                    gameData.id, pending.cardId());
            return;
        }
        if (gameData.graveyardEntryVersion(cardToReturn.getId()) != pending.graveyardEntryVersion()) {
            // The card left the graveyard and came back, so this is a new object the delayed
            // return was not scheduled for (CR 400.7).
            log.info("Game {} - Delayed return under control for card {} skipped (new graveyard object)",
                    gameData.id, pending.cardId());
            return;
        }
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
            return;
        }

        Permanent sourcePermanent = pending.sourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        String sourceName = sourcePermanent != null ? sourcePermanent.getCard().getName() : "Delayed return";

        // Shirei: "if Shirei is still on the battlefield". The permanent id is matched, so a
        // Shirei that left and returned is a new object and the return does not happen.
        if (pending.requireSourceOnBattlefield() && sourcePermanent == null) {
            log.info("Game {} - Delayed return under control for card {} skipped (source left the battlefield)",
                    gameData.id, pending.cardId());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
        Permanent permanent = new Permanent(cardToReturn);
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        // "with an additional +1/+1 counter on it" / "is a black Zombie in addition to its
        // other colors and types" (Grave Betrayal) — applied before the permanent enters.
        if (pending.counterType() != null && pending.counterAmount() > 0) {
            permanent.setCounterCount(pending.counterType(), pending.counterAmount());
        }
        if (pending.grantColor() != null) {
            permanent.getGrantedColors().add(pending.grantColor());
        }
        if (pending.grantSubtype() != null && !permanent.getGrantedSubtypes().contains(pending.grantSubtype())) {
            permanent.getGrantedSubtypes().add(pending.grantSubtype());
        }
        UUID returnControllerId = pending.returnUnderOwnersControl() ? ownerId : pending.controllerId();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, permanent);

        // When the returned card belongs to another player, the controller keeps it via a
        // permanent control effect (CR 613 layer 2) — without one it would revert to its owner
        // — and its ownership is recorded so it dies to its owner's graveyard.
        if (!pending.returnUnderOwnersControl() && !returnControllerId.equals(ownerId)) {
            gameData.stolenCreatures.put(permanent.getId(), ownerId);
            creatureControlService.applyControlEffect(gameData, returnControllerId, permanent,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    ControlDuration.PERMANENT.toEffectDuration(), null, sourceName);
        }

        String playerName = gameData.playerIdToName.get(returnControllerId);
        gameLogService.append(gameData, GameLog.cardThen(cardToReturn,
                " returns to the battlefield under " + playerName + "'s control (" + sourceName + ")."));
        log.info("Game {} - {} returns under {}'s control ({})", gameData.id, cardToReturn.getName(), playerName, sourceName);
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, cardToReturn, null, false);

        // Link to the Seraph for the control-loss sacrifice, but only if it is still on the
        // battlefield — if it already left, you never have to sacrifice the returned creature.
        if (!pending.returnUnderOwnersControl() && pending.sacrificeOnSourceControlLoss()
                && sourcePermanent != null) {
            gameData.seraphReturnedCreatures
                    .computeIfAbsent(pending.sourcePermanentId(), k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(permanent.getId());
            gameData.seraphControlWatch.putIfAbsent(pending.sourcePermanentId(),
                    gameQueryService.findPermanentController(gameData, sourcePermanent.getId()));
        }
    }
}
