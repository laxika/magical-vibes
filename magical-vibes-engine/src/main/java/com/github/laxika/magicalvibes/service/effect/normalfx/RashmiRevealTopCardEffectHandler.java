package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RashmiRevealTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RashmiRevealTopCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RashmiRevealTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + "'s library is empty ("
                            + entry.getCard().getName() + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + entry.getCard().getName() + ").")
                .build());

        // "That spell's mana value": the triggering spell's, as it last existed on the stack once it has left (CR 608.2h).
        int spellManaValue = amountEvaluationService.evaluate(gameData, new TargetSpellManaValue(),
                AmountContext.forStackEntry(entry, null));
        if (topCard.hasType(CardType.LAND) || topCard.getManaValue() >= spellManaValue) {
            deck.removeFirst();
            gameData.addCardToHand(controllerId, topCard);
            gameLogService.append(gameData, GameLog.cardThen(topCard,
                    " is put into " + gameData.playerIdToName.get(controllerId) + "'s hand."));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(new RevealTopCardMayPlayFreeEffect(LookDestination.HAND)),
                entry.getCard().getName() + " — Cast " + topCard.getName()
                        + " without paying its mana cost?"));
        log.info("Game {} - {} offers {} for Rashmi's free cast",
                gameData.id, gameData.playerIdToName.get(controllerId), topCard.getName());
    }
}
