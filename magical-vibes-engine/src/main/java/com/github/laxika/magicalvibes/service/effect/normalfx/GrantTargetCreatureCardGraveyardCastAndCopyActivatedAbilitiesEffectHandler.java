package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in graveyard)."));
            return;
        }
        if (!targetCard.hasType(CardType.CREATURE)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target is not a creature card)."));
            return;
        }

        gameData.graveyardCreatureCastPermissionsUntilEndOfTurn.put(targetCard.getId(),
                new GameData.GraveyardCreatureCastPermission(entry.getSourcePermanentId(), entry.getControllerId()));

        String logEntry = entry.getCard().getName() + " allows " + targetCard.getName()
                + " to be cast from a graveyard this turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), " allows ", targetCard, " to be cast from a graveyard this turn."));
        log.info("Game {} - {} grants graveyard cast permission for {}", gameData.id, entry.getCard().getName(), targetCard.getName());
    }
}
