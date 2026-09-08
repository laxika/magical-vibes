package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEmbalmToTargetCreatureCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantEmbalmToTargetCreatureCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEmbalmToTargetCreatureCardEffect.class;
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
        if (targetCard.getManaCost() == null || targetCard.getManaCost().isBlank()) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target has no mana cost)."));
            return;
        }

        gameData.cardsGrantedEmbalmUntilEndOfTurn.add(targetCard.getId());
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " grants embalm to ", targetCard, " until end of turn."));
        log.info("Game {} - {} grants embalm to {}", gameData.id, entry.getCard().getName(), targetCard.getName());
    }
}
