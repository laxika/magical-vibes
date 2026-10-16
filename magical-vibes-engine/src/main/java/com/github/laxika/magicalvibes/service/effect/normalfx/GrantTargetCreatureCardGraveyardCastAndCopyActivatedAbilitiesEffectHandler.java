package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds().getFirst()
                : entry.getTargetId();
        if (targetCardId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in graveyard)."));
            return;
        }
        if (!targetCard.hasType(CardType.CREATURE)) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target is not a creature card)."));
            return;
        }

        GameData.GraveyardCardCastPermission grant = new GameData.GraveyardCardCastPermission(
                entry.getSourcePermanentId(), entry.getControllerId(), true, false);
        gameData.graveyardCardCastPermissionGrantsUntilEndOfTurn
                .computeIfAbsent(targetCard.getId(), ignored -> new CopyOnWriteArrayList<>()).add(grant);
        GameData.GraveyardCardCastPermission existing =
                gameData.graveyardCardCastPermissionsUntilEndOfTurn.get(targetCard.getId());
        if (existing == null || existing.castingPlayerId().equals(entry.getControllerId())) {
            gameData.graveyardCardCastPermissionsUntilEndOfTurn.put(targetCard.getId(), grant);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " allows ", targetCard, " to be cast from a graveyard this turn."));
        log.info("Game {} - {} grants graveyard cast permission for {}", gameData.id, entry.getCard().getName(), targetCard.getName());
    }
}
