package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayDealDamageOrPlayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ExileTopCardMayDealDamageOrPlayEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    public ExileTopCardMayDealDamageOrPlayEffectHandler(ExileService exileService,
                                                         GameLogService gameLogService,
                                                         @Lazy PlayerInputService playerInputService) {
        this.exileService = exileService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayDealDamageOrPlayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new ExileTopCardMayDealDamageOrPlayEffect(topCard.getId(), entry.getTargetId())),
                entry.getCard().getName()
                        + " — Have it deal damage to the target creature equal to the exiled card's mana value?"
        ));
        gameLogService.append(gameData, GameLog.cardThen(topCard,
                " is exiled from the top of the library."));
        playerInputService.processNextMayAbility(gameData);
    }
}
