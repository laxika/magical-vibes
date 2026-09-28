package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardIfOddMayCastFreeElseDrawEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardIfOddMayCastFreeElseDrawEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardIfOddMayCastFreeElseDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + entry.getCard().getName() + ").")
                .build());

        if (topCard.hasType(CardType.LAND) || topCard.getManaValue() % 2 == 0) {
            drawService.resolveDrawCard(gameData, controllerId);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(new RevealTopCardMayPlayFreeEffect(LookDestination.HAND, false, null, true)),
                entry.getCard().getName() + " — Cast " + topCard.getName() + " without paying its mana cost?",
                null,
                null,
                entry.getSourcePermanentId()
        ));
        log.info("Game {} - {} may cast odd-mana-value {} for free",
                gameData.id, gameData.playerIdToName.get(controllerId), topCard.getName());
    }
}
