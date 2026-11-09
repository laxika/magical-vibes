package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Stores a global trigger through the end of its controller's next turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect) effect;
        // The registration records the registering source permanent, the player the resolving ability names (its
        // target, or the defending player of an attack trigger) and the card its source exiled with its linked
        // ability, whatever the ability's effects are; the ability's filters and effects read them (CR 603.7c, 603.7e).
        UUID watchedPlayerId = resolveTargetPlayerId(gameData, entry);
        UUID expirationPlayerId = registration.untilWatchedPlayersNextTurn() && watchedPlayerId != null
                ? watchedPlayerId : entry.getControllerId();
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), (TriggeredAbility) registration.grantedAbility(),
                registration.targetFilter(), true, false, gameData.turnNumber, expirationPlayerId, null,
                entry.getSourcePermanentId(), watchedPlayerId,
                RegisterGlobalTriggeredAbilityUntilEndOfTurnEffectHandler.linkedExiledCardId(gameData, entry)));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability until the end of their next turn."));
        log.info("Game {} - {} registers a global triggered ability until the end of their next turn",
                gameData.id, entry.getCard().getName());
    }

    private UUID resolveTargetPlayerId(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getAttackedTargetId() != null
                ? entry.getAttackedTargetId() : entry.getTargetId();
        if (targetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(targetId)) {
            return targetId;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (controllerId == null) {
            controllerId = entry.getRemovedPermanentControllers().get(targetId);
        }
        if (controllerId == null && targetId.equals(entry.getAttackedTargetId())) {
            controllerId = entry.getDefendingPlayerId();
        }
        return controllerId;
    }
}
