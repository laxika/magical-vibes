package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealForManaAndPutIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealForManaAndPutIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealForManaAndPutIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealForManaAndPutIntoHandEffect e =
                (LookAtTopCardMayRevealForManaAndPutIntoHandEffect) effect;
        if (e.stage() != LookAtTopCardMayRevealForManaAndPutIntoHandEffect.Stage.LOOK) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + "'s library is empty ("
                            + entry.getCard().getName() + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " looks at the top card of their library ("
                        + entry.getCard().getName() + ")."));

        List<ManaColor> colors = coloredManaColors(topCard);
        int coloredSymbols = topCard.getParsedManaCost() == null
                ? 0
                : topCard.getParsedManaCost().countSymbolsOfAnyColor(Set.copyOf(ManaColor.COLORS));
        if (coloredSymbols < 3 || colors.isEmpty()) {
            putTopCardIntoHand(gameData, controllerId, entry.getCard().getName());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(e.withMayRevealStage(colors)),
                entry.getCard().getName() + " — Reveal the top card and add three mana?"));
        log.info("Game {} - {} may reveal {} for mana via {}",
                gameData.id, gameData.playerIdToName.get(controllerId), topCard.getName(), entry.getCard().getName());
    }

    static List<ManaColor> coloredManaColors(Card card) {
        if (card.getParsedManaCost() == null) {
            return List.of();
        }
        return ManaColor.COLORS.stream()
                .filter(color -> card.getParsedManaCost().countColorSymbols(color) > 0)
                .toList();
    }

    private void putTopCardIntoHand(GameData gameData, UUID playerId, String sourceName) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }
        Card topCard = deck.removeFirst();
        gameData.addCardToHand(playerId, topCard);
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(playerId) + " puts the top card of their library into their hand ("
                        + sourceName + ").")
                .build());
    }
}
