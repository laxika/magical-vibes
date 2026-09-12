package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCreatureFromTargetOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PutRandomCreatureFromTargetOpponentGraveyardOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutRandomCreatureFromTargetOpponentGraveyardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || controllerId.equals(targetPlayerId)) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard == null) {
            return;
        }

        List<Card> creatureCards = graveyard.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (creatureCards.isEmpty()) {
            return;
        }

        Card card = creatureCards.get(ThreadLocalRandom.current().nextInt(creatureCards.size()));
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromGraveyardOwnerId(targetPlayerId);

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, controllerId, permanent, enterTappedTypes);
        graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, targetPlayerId);

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " puts ")
                .card(card)
                .text(" onto the battlefield under their control.")
                .build());
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }
}
