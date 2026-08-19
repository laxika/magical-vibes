package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class ReturnDyingCreatureToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnDyingCreatureToBattlefieldEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Find the dying card in a graveyard
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, e.dyingCardId());
        if (dyingCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card is no longer in graveyard)."));
            log.info("Game {} - Return+attach fizzles, card not in {}'s graveyard", gameData.id, playerName);
            return;
        }

        // Remove from graveyard
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCard.getId());

        // Put onto the battlefield
        Permanent creature = new Permanent(dyingCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, creature);

        
        gameLogService.append(gameData, GameLog.builder().card(dyingCard).text(" returns to the battlefield under " + playerName + "'s control.").build());
        log.info("Game {} - {} returns {} to battlefield via {}", gameData.id, playerName, dyingCard.getName(), entry.getCard().getName());

        // Attach the source equipment to the returned creature
        UUID equipmentId = entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId();
        Permanent equipment = e.attachSource() && equipmentId != null
                ? gameQueryService.findPermanentById(gameData, equipmentId)
                : null;
        if (equipment != null) {
            UUID oldAttachedTo = equipment.getAttachedTo();
            gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
            equipment.setAttachedTo(creature.getId());
            // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, creature.getId());
            
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " is now attached to ", dyingCard, "."));
            log.info("Game {} - {} attached to {}", gameData.id, entry.getCard().getName(), dyingCard.getName());
        }

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, creature, dyingCard);
    }
}
