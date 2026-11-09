package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stores a global trigger registered by a resolving spell until turn cleanup. The trigger remains
 * active if the registering spell has already moved to its owner's graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect) effect;
        TriggeredAbility ability = (TriggeredAbility) registration.grantedAbility();
        // The registration records the registering source permanent, the player the resolving ability targets and
        // the card its source exiled with its linked ability, whatever the ability's effects are; the ability's
        // filters and effects read them (CR 603.7c, 603.7e).
        UUID watchedPlayerId = entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                ? entry.getTargetId() : null;
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), ability, null, false, false, -1, null, null,
                entry.getSourcePermanentId(), watchedPlayerId, linkedExiledCardId(gameData, entry)));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability until end of turn."));
        log.info("Game {} - {} registers a global triggered ability until end of turn",
                gameData.id, entry.getCard().getName());
    }

    /**
     * The card the resolving ability's source exiled with its linked ability (a resolving spell's own exiled card,
     * Surge to Victory), as it is now, or {@code null}.
     */
    static UUID linkedExiledCardId(GameData gameData, StackEntry entry) {
        Card exiled = gameData.getImprintedCard(entry.getCard());
        return exiled == null ? null : exiled.getId();
    }
}
