package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect} (Rotfeaster
 * Maggot): capture the graveyard target's printed toughness, exile it, then gain that much life.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Trigger path (ETB) carries the chosen graveyard card in targetCardIds; a spell/ability path
        // would carry it in targetId.
        UUID targetCardId = entry.getTargetCardIds() == null || entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId() : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        int toughness = targetCard.getToughness() == null ? 0 : Math.max(0, targetCard.getToughness());

        graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard);

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " exiles ", targetCard, " from a graveyard."));

        if (toughness > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, toughness,
                    entry.getCard().getName(), entry.getCard(), entry.getEntryType());
        }
    }
}
