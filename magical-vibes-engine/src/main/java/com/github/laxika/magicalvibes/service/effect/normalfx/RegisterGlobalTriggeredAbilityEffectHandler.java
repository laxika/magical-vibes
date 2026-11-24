package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stores a global trigger registered by a resolving spell or ability until turn cleanup, or until the beginning of its
 * controller's next turn, or, of a "when you next ..." one, until it first triggers. The trigger remains active if the registering spell has already moved to its owner's
 * graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityEffect) effect;
        TriggeredAbility ability = (TriggeredAbility) registration.grantedAbility();
        // The registration records the registering source permanent, the player the resolving ability targets and
        // the card its source exiled with its linked ability, whatever the ability's effects are; the ability's
        // filters and effects read them (CR 603.7c, 603.7e).
        UUID watchedPlayerId = entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                ? entry.getTargetId() : null;
        // "Whenever a creature attacks you or <this permanent>" (Tasha, Unholy Archmage) watches the registering
        // permanent, so attacks on other planeswalkers the player controls don't count.
        UUID watchedPermanentId = ability.hasWatchedTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker())
                ? entry.getSourcePermanentId() : null;
        boolean untilNextTurn = Duration.UNTIL_YOUR_NEXT_TURN.equals(registration.duration());
        // A "when you next ..." one is used up the first time it triggers (CR 603.7b); each keeps the number the
        // resolution noted ("copy that spell X times").
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), ability, null, false, untilNextTurn,
                untilNextTurn ? gameData.turnNumber : -1, null, watchedPermanentId,
                entry.getSourcePermanentId(), watchedPlayerId, linkedExiledCardId(gameData, entry), false,
                registration.next()).keepingNotedAmount(entry.getResolutionResults()));
        String until = untilNextTurn ? "until their next turn" : "until end of turn";
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability " + until + "."));
        log.info("Game {} - {} registers a global triggered ability {}", gameData.id, entry.getCard().getName(), until);
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
