package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " exiles ").card(topCard)
                .text(" from the top of their library and may play it this turn.").build());

        if (!topCard.hasType(CardType.LAND)) {
            queueReflexiveAbilityEffectHandler.resolve(gameData, entry,
                    new QueueReflexiveAbilityEffect(
                            new DealDamageToAnyTargetEffect(topCard.getManaValue())));
        }
    }
}
