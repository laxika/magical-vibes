package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfEnteringCreatureHasUniqueNameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Resolves Guardian Project's same-name intervening condition and draw. */
@Component
@RequiredArgsConstructor
public class DrawCardIfEnteringCreatureHasUniqueNameEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DrawCardEffectHandler drawCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardIfEnteringCreatureHasUniqueNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardIfEnteringCreatureHasUniqueNameEffect draw =
                (DrawCardIfEnteringCreatureHasUniqueNameEffect) effect;
        String enteringName = currentEnteringName(gameData, entry, draw);
        if (hasSameNameOnBattlefield(gameData, entry, enteringName)
                || hasSameNameInGraveyard(gameData, entry.getControllerId(), enteringName)) {
            return;
        }

        drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect());
    }

    private String currentEnteringName(GameData gameData, StackEntry entry,
                                       DrawCardIfEnteringCreatureHasUniqueNameEffect effect) {
        if (entry.getTriggeringPermanentId() != null) {
            Permanent entering = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
            if (entering != null) {
                return gameQueryService.getEffectiveName(gameData, entering);
            }
        }

        UUID enteringCardId = effect.enteringCardId() != null
                ? effect.enteringCardId() : entry.getTriggeringCardId();
        Card enteringCard = gameQueryService.findCardInGraveyardById(gameData, enteringCardId);
        if (enteringCard == null) {
            enteringCard = gameQueryService.findCardInExileById(gameData, enteringCardId);
        }
        return enteringCard == null ? effect.enteringCardName() : enteringCard.getName();
    }

    private boolean hasSameNameOnBattlefield(GameData gameData, StackEntry entry, String enteringName) {
        if (enteringName == null) {
            return false;
        }

        List<Permanent> battlefield = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of());
        UUID enteringPermanentId = entry.getTriggeringPermanentId();
        return battlefield.stream()
                .filter(permanent -> enteringPermanentId == null
                        || !enteringPermanentId.equals(permanent.getId()))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(permanent -> gameQueryService.getEffectiveName(gameData, permanent))
                .anyMatch(name -> Objects.equals(name, enteringName));
    }

    private boolean hasSameNameInGraveyard(GameData gameData, UUID controllerId, String enteringName) {
        if (enteringName == null) {
            return false;
        }

        return gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .anyMatch(card -> Objects.equals(card.getName(), enteringName));
    }
}
