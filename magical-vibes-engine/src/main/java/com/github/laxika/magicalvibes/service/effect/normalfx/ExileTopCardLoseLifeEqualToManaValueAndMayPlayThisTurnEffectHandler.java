package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardLoseLifeEqualToManaValueAndMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hotel of Fears: exile the top card of the controller's library, the controller loses life equal to its mana value
 * and may play that card this turn.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: exiling a card from a library can't change what a Lich's Duel
 * Mastery's sacrifice can be replaced with, so the question that sacrifice needs (CR 616.1) is asked before the
 * exile, while nothing has changed yet, and the effect is resolved again with the answer; the loss itself still
 * follows the exile (CR 608.2c).
 */
@Component
@RequiredArgsConstructor
public class ExileTopCardLoseLifeEqualToManaValueAndMayPlayThisTurnEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final ExileService exileService;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardLoseLifeEqualToManaValueAndMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.getFirst();
        Map<UUID, Integer> losses = Map.of(controllerId, topCard.getManaValue());
        lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, losses);

        library.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        lifeSupport.applyLifeLosses(gameData, losses, entry.getCard().getName());
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                .card(topCard)
                .text(" from the top of their library and may play it this turn.")
                .build());
    }
}
