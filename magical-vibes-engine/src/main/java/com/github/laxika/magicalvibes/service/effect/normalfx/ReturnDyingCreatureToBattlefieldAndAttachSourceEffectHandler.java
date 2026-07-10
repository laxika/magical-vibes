package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnDyingCreatureToBattlefieldAndAttachSourceEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToBattlefieldAndAttachSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnDyingCreatureToBattlefieldAndAttachSourceEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Find the dying card in a graveyard
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, e.dyingCardId());
        if (dyingCard == null) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (card is no longer in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Return+attach fizzles, card not in {}'s graveyard", gameData.id, playerName);
            return;
        }

        // Remove from graveyard
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCard.getId());

        // Put onto the battlefield
        Permanent creature = new Permanent(dyingCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, creature);

        String enterLog = dyingCard.getName() + " returns to the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, enterLog);
        log.info("Game {} - {} returns {} to battlefield via {}", gameData.id, playerName, dyingCard.getName(), entry.getCard().getName());

        // Attach the source equipment to the returned creature
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (equipment != null) {
            equipment.setAttachedTo(creature.getId());
            // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
            equipment.setTimestamp(gameData.nextTimestamp());
            String attachLog = entry.getCard().getName() + " is now attached to " + dyingCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, attachLog);
            log.info("Game {} - {} attached to {}", gameData.id, entry.getCard().getName(), dyingCard.getName());
        }

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, creature, dyingCard);
    }
}
