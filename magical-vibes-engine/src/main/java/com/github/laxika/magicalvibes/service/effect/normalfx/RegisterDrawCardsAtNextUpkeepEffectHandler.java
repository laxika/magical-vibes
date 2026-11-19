package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToRecipient;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDrawCardsAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDrawCardsAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDrawCardsAtNextUpkeepEffect) effect;
        UUID drawerId = switch (e.recipient()) {
            case CONTROLLER -> entry.getControllerId();
            case TARGET_PLAYER -> entry.getTargetId();
            case TARGET_SPELL_CONTROLLER -> findTargetSpellControllerId(gameData, entry.getTargetId());
            case TARGET_GRAVEYARD_OWNER -> findTargetGraveyardOwnerId(gameData, entry);
        };
        if (drawerId == null) {
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(drawAtNextTurnsUpkeep(gameData.turnNumber,
                entry.getControllerId(), drawerId, e.count(), e.upTo(), entry.getCard(),
                entry.getSourcePermanentId()));

        String playerName = gameData.playerIdToName.get(drawerId);
        log.info("Game {} - {} registers delayed draw of {} at next upkeep", gameData.id, playerName, e.count());
    }

    /**
     * The delayed triggered ability "at the beginning of the next turn's upkeep, {@code drawerId} draws {@code count}
     * cards" ("up to" that many when {@code upTo}) that a spell or ability of {@code sourceCard} controlled by
     * {@code controllerId} made on turn {@code turnNumber}: it triggers at the next upkeep of a later turn, whoever's
     * it
     * is, and its controller is its maker's, whoever draws (CR 603.7d).
     */
    public static TemporaryGlobalTriggeredAbility drawAtNextTurnsUpkeep(int turnNumber, UUID controllerId,
                                                                        UUID drawerId, int count, boolean upTo,
                                                                        Card sourceCard, UUID sourcePermanentId) {
        boolean drawerIsController = drawerId.equals(controllerId);
        CardEffect draw = upTo ? new DrawUpToNCardsEffect(count, DrawUpToRecipient.TARGET_PLAYER)
                : drawerIsController ? new DrawCardEffect(count)
                : new DrawCardEffect(count, Subjects.targetPlayerDeclaredElsewhere());
        return TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                new TriggeredAbility(Triggers.upkeep(ActivePlayer.ANY), draw), sourcePermanentId, null,
                upTo || !drawerIsController ? drawerId : null, DelayedReferents.NONE.afterTurn(turnNumber));
    }

    /**
     * Owner of the graveyard holding this entry's first still-present targeted card, or null when
     * every chosen target has left its graveyard. With zero targets chosen a player is still chosen
     * (Lodestone Bauble ruling), which defaults to the entry's player target or else its controller.
     */
    private UUID findTargetGraveyardOwnerId(GameData gameData, StackEntry entry) {
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetCardIds == null) {
            return null;
        }
        if (targetCardIds.isEmpty()) {
            return entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                    ? entry.getTargetId() : entry.getControllerId();
        }
        for (UUID cardId : targetCardIds) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (ownerId != null) {
                return ownerId;
            }
        }
        return null;
    }

    /** Controller of the spell on the stack whose card id matches {@code targetCardId}, or null. */
    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(targetCardId)) {
                return se.getControllerId();
            }
        }
        return null;
    }
}
