package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnGraveyardCardsToBattlefieldUnderControlEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Returns the remembered cards to the battlefield simultaneously under the trigger controller's
 * control, skipping each card that is no longer in the graveyard stay (or exile) it was remembered in.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnGraveyardCardsToBattlefieldUnderControlEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnGraveyardCardsToBattlefieldUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnGraveyardCardsToBattlefieldUnderControlEffect) effect;
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();

        List<ReturningCard> returningCards = new ArrayList<>();
        for (UUID cardId : returnEffect.cardIds()) {
            Long expectedVersion = returnEffect.graveyardEntryVersions().get(cardId);
            Card card;
            UUID ownerId;
            Zone from;
            if (expectedVersion != null) {
                card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                from = Zone.GRAVEYARD;
                if (card == null || ownerId == null || gameData.graveyardEntryVersion(cardId) != expectedVersion) {
                    log.info("Game {} - Delayed return for card {} skipped (no longer in the remembered graveyard)",
                            gameData.id, cardId);
                    continue;
                }
            } else {
                var exiled = gameData.findExiledCard(cardId);
                if (exiled == null) {
                    log.info("Game {} - Delayed return for card {} skipped (no longer in exile)",
                            gameData.id, cardId);
                    continue;
                }
                card = exiled.card();
                ownerId = exiled.ownerId();
                from = Zone.EXILE;
            }
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, from)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(card, " can't return; it stays where it is."));
                continue;
            }
            returningCards.add(new ReturningCard(card, ownerId, from));
        }
        if (returningCards.isEmpty()) {
            return;
        }

        for (ReturningCard returning : returningCards) {
            if (returning.from() == Zone.GRAVEYARD) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, returning.card().getId());
            } else {
                gameData.removeFromExile(returning.card().getId());
            }
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        for (ReturningCard returning : returningCards) {
            Card card = returning.card();
            Permanent permanent = new Permanent(card);
            if (returning.from() == Zone.GRAVEYARD) {
                permanent.setEnteredFromGraveyardOwnerId(returning.ownerId());
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);

            if (!controllerId.equals(returning.ownerId())) {
                gameData.stolenCreatures.put(permanent.getId(), returning.ownerId());
                creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        ControlDuration.PERMANENT.toEffectDuration(), null, sourceName);
            }

            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " returns to the battlefield under " + playerName + "'s control (" + sourceName + ")."));
            log.info("Game {} - {} returns under {}'s control ({})",
                    gameData.id, card.getName(), playerName, sourceName);
        }
        for (ReturningCard returning : returningCards) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, returning.card(), null, false);
        }
    }

    private record ReturningCard(Card card, UUID ownerId, Zone from) {}
}
