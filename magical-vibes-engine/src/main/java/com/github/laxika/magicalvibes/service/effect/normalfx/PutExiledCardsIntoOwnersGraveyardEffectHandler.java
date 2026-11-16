package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsStillInZone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Moves each remembered card that is still face-up in exile to its owner's graveyard. */
@Component
@RequiredArgsConstructor
public class PutExiledCardsIntoOwnersGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutExiledCardsIntoOwnersGraveyardEffect.class;
    }

    /**
     * The delayed triggered ability "at the beginning of the next end step, if any of those cards remain exiled, put
     * them into their owners' graveyards" that a spell or ability of {@code sourceCard} controlled by
     * {@code controllerId} made after exiling {@code cardIds}.
     */
    public static TemporaryGlobalTriggeredAbility atNextEndStep(GameData gameData, UUID controllerId, Card sourceCard,
                                                                UUID sourcePermanentId, List<UUID> cardIds) {
        Map<UUID, Long> versions = gameData.exileEntryVersionsOf(cardIds);
        return TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                        TriggerOptions.DEFAULT.withInterveningIf(new CardsStillInZone(Zone.EXILE, versions)),
                        new PutExiledCardsIntoOwnersGraveyardEffect(versions)),
                sourcePermanentId, null, null, DelayedReferents.cards(cardIds));
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (var remembered : ((PutExiledCardsIntoOwnersGraveyardEffect) effect).exileEntryVersions().entrySet()) {
            UUID cardId = remembered.getKey();
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null || exiled.faceDown()
                    || !remembered.getValue().equals(gameData.exileEntryVersions.getOrDefault(cardId, 0L))
                    || !gameData.removeFromExile(cardId)) {
                continue;
            }
            graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getDescription() + " puts ", exiled.card(),
                            " into its owner's graveyard."));
        }
    }
}
