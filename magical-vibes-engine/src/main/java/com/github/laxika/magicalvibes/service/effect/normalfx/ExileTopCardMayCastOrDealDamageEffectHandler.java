package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayCastOrDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ExileTopCardMayCastOrDealDamageEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final DealDamageEffectHandler dealDamageEffectHandler;

    public ExileTopCardMayCastOrDealDamageEffectHandler(ExileService exileService,
                                                         GameLogService gameLogService,
                                                         @Lazy PlayerInputService playerInputService,
                                                         DealDamageEffectHandler dealDamageEffectHandler) {
        this.exileService = exileService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.dealDamageEffectHandler = dealDamageEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayCastOrDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardMayCastOrDealDamageEffect exileEffect =
                (ExileTopCardMayCastOrDealDamageEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            dealDamageEffectHandler.resolve(gameData, entry,
                    DealDamageEffect.to(Subjects.eachOpponent(), exileEffect.damage()));
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        if (topCard.hasType(com.github.laxika.magicalvibes.model.CardType.LAND)) {
            dealDamageEffectHandler.resolve(gameData, entry,
                    DealDamageEffect.to(Subjects.eachOpponent(), exileEffect.damage()));
            return;
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(exileEffect),
                "Cast " + topCard.getName() + " for its mana cost?",
                topCard.getId()
        ));
        gameLogService.append(gameData, GameLog.cardThen(topCard, " is exiled from the top of the library."));
        log.info("Game {} - {} exiles {} from the top of the library for a may-cast choice",
                gameData.id, gameData.playerIdToName.get(controllerId), topCard.getName());
        playerInputService.processNextMayAbility(gameData);
    }
}
