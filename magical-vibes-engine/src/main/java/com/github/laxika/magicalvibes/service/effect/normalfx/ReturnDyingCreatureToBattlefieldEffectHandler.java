package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnDyingCreatureToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final DurationClock durationClock;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final EquipSupport equipSupport;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

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
        Card dyingCard = entry.getTriggeringCardId() == null ? null
                : gameQueryService.findCardInGraveyardById(gameData, entry.getTriggeringCardId());
        if (dyingCard != null && !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(dyingCard.getId()))) {
            dyingCard = null;
        }
        if (dyingCard != null && gameQueryService.cantEnterFaceUp(dyingCard)) {
            return;
        }
        if (dyingCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card is no longer in graveyard)."));
            log.info("Game {} - Return+attach fizzles, card not in {}'s graveyard", gameData.id, playerName);
            return;
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCard.getId());

        Permanent creature = new Permanent(dyingCard);
        CounterType enterWithCounter = e.enterWithCounter();
        if (enterWithCounter != null && e.enterWithCounterCount() > 0) {
            creature.setCounterCount(enterWithCounter,
                    creature.getCounterCount(enterWithCounter) + e.enterWithCounterCount());
        }
        creature.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
        if (dyingCard.isAura()) {
            // An Aura card (one that died animated) enters attached to what its new controller chooses, or stays in
            // the graveyard with nothing to enchant (CR 303.4f, 303.4g); no Equipment can be attached to it.
            battlefieldEntryBatchSupport.begin(gameData, List.of(new BattlefieldEntryCard(controllerId,
                    graveyardOwnerId, dyingCard, Zone.GRAVEYARD, null, null, creature, List.of(), false,
                    PutCardsOntoBattlefieldEffect.EnteringAs.NOTHING_ELSE, null, null)));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCard.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, creature);
        if (graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
            graveyardReturnSupport.trackStolenCreature(gameData, creature.getId(), controllerId, graveyardOwnerId);
        }

        
        gameLogService.append(gameData, GameLog.builder().card(dyingCard).text(" returns to the battlefield under " + playerName + "'s control.").build());
        log.info("Game {} - {} returns {} to battlefield via {}", gameData.id, playerName, dyingCard.getName(), entry.getCard().getName());

        // Attach the source equipment to the returned creature
        UUID equipmentId = entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId();
        Permanent equipment = e.attachSource() && equipmentId != null
                ? gameQueryService.findPermanentById(gameData, equipmentId)
                : null;
        if (equipment != null && !equipSupport.canAttachEquipment(gameData, equipment, creature)) {
            equipment = null;
        }
        if (equipment != null) {
            UUID oldAttachedTo = equipment.getAttachedTo();
            durationClock.sourceUnattached(gameData, equipment.getId());
            equipment.setAttachedTo(creature.getId());
            // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, creature.getId());
            equipment.setAttachedTo(creature.getId());
            // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
            
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " is now attached to ", dyingCard, "."));
            log.info("Game {} - {} attached to {}", gameData.id, entry.getCard().getName(), dyingCard.getName());
        }

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, creature, dyingCard);
    }
}
