package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Lucky Slots' top-card exile, play permission, and target-creature boost. */
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final BoostTargetCreatureEffectHandler boostTargetCreatureEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(controllerId)
                            + "'s library is empty - nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " exiles ").card(topCard)
                .text(" from the top of their library (may play it this turn).").build());

        entry.setEventValue(topCard.getManaValue());
        boostTargetCreatureEffectHandler.resolve(gameData, entry,
                new BoostTargetCreatureEffect(new EventValue(), new Fixed(0)));
    }
}
