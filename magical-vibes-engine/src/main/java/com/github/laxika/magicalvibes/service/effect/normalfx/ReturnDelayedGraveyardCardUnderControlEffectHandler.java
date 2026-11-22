package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDelayedGraveyardCardUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
import com.github.laxika.magicalvibes.model.Zone;

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
        var returnEffect = (ReturnDelayedGraveyardCardUnderControlEffect) effect;
        UUID cardId = entry.getTriggeringCardId();
        UUID ownerId = null;
        Card cardToReturn = null;
        if (cardId != null) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Card> graveyard = gameData.playerGraveyards.get(pid);
                if (graveyard == null) continue;
                for (Card card : graveyard) {
                    if (card.getId().equals(cardId)) {
                        cardToReturn = card;
                        ownerId = pid;
                        break;
                    }
                }
                if (cardToReturn != null) break;
            }
        }
        if (cardToReturn == null) {
            // No longer in a graveyard (moved/exiled/reanimated already, or it was a token) —
            // you don't get it back.
            log.info("Game {} - Delayed return under control for card {} skipped (no longer in a graveyard)",
                    gameData.id, cardId);
            return;
        }
        if (!entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(cardToReturn.getId()))) {
            // The card left the graveyard and came back, so this is a new object the delayed
            // return was not made for (CR 400.7).
            log.info("Game {} - Delayed return under control for card {} skipped (new graveyard object)",
                    gameData.id, cardId);
            return;
        }
        if (gameQueryService.cantEnterFaceUp(cardToReturn)) {
            return;
        }
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        String sourceName = entry.getCard().getName();

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
        Permanent permanent = new Permanent(cardToReturn);
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        // "with an additional +1/+1 counter on it" / "is a black Zombie in addition to its
        // other colors and types" (Grave Betrayal) — applied before the permanent enters.
        UUID returnControllerId = returnEffect.returnControllerId();
        if (returnEffect.counterType() != null && returnEffect.counterAmount() > 0) {
            int placedCounters = gameQueryService.replaceCounters(gameData, permanent, returnControllerId,
                    returnEffect.counterType(), returnEffect.counterAmount());
            if (placedCounters > 0) {
                permanent.setCounterCount(returnEffect.counterType(), placedCounters);
            }
        }
        if (returnEffect.grantColor() != null) {
            gameData.addEffectOnPermanent(entry, new GrantColorEffect(returnEffect.grantColor(), Subjects.target()),
                    permanent.getId(), Duration.PERMANENT);
        }
        if (returnEffect.grantSubtype() != null) {
            gameData.addEffectOnPermanent(entry,
                    new GrantSubtypeEffect(returnEffect.grantSubtype(), Subjects.target()),
                    permanent.getId(), Duration.PERMANENT);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, permanent);

        // When the returned card belongs to another player, the controller keeps it via a
        // permanent control effect (CR 613 layer 2) — without one it would revert to its owner
        // — and its ownership is recorded so it dies to its owner's graveyard.
        if (!returnControllerId.equals(ownerId)) {
            gameData.stolenCreatures.put(permanent.getId(), ownerId);
            creatureControlService.applyControlEffect(gameData, returnControllerId, permanent,
                    new GainControlOfTargetEffect(Duration.PERMANENT),
                    Duration.PERMANENT, null, sourceName);
        }

        String playerName = gameData.playerIdToName.get(returnControllerId);
        gameLogService.append(gameData, GameLog.cardThen(cardToReturn,
                " returns to the battlefield under " + playerName + "'s control (" + sourceName + ")."));
        log.info("Game {} - {} returns under {}'s control ({})", gameData.id, cardToReturn.getName(), playerName, sourceName);
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, cardToReturn, null, false);

        // Link to the Seraph for the control-loss sacrifice, but only if it is still on the
        // battlefield — if it already left, you never have to sacrifice the returned creature.
        if (returnEffect.sacrificeOnSourceControlLoss() && sourcePermanent != null) {
            gameData.seraphReturnedCreatures
                    .computeIfAbsent(sourcePermanentId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(permanent.getId());
            gameData.seraphControlWatch.putIfAbsent(sourcePermanentId,
                    gameQueryService.findPermanentController(gameData, sourcePermanent.getId()));
            gameData.seraphLinkedController.putIfAbsent(sourcePermanentId, returnControllerId);
        }
    }
}
