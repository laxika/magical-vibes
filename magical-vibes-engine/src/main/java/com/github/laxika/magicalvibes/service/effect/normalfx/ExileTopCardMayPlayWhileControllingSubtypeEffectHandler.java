package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileControllingSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPlayWhileControllingSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayWhileControllingSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardMayPlayWhileControllingSubtypeEffect exileEffect =
                (ExileTopCardMayPlayWhileControllingSubtypeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionConditions.put(topCard.getId(),
                new ControlsPermanent(new PermanentHasSubtypePredicate(exileEffect.subtype())));

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " exiles " + topCard.getName()
                + " from the top of their library and may play it while controlling a "
                + exileEffect.subtype().getDisplayName() + "."));
        log.info("Game {} - {} exiles {} from library top and may play it while controlling a {}",
                gameData.id, controllerName, topCard.getName(), exileEffect.subtype().getDisplayName());
    }
}
