package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForVividEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the library reveal and starts Vivid's per-color choices. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsForVividEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final VividRevealSupport vividRevealSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsForVividEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsForVividEffect vivid = (RevealTopCardsForVividEffect) effect;
        UUID controllerId = entry.getControllerId();
        int requiredNonlands = amountEvaluationService.evaluate(
                gameData, vivid.nonlandCount(), AmountContext.forStackEntry(entry, null));

        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> revealed = new ArrayList<>();
        int nonlands = 0;
        while (deck != null && !deck.isEmpty() && nonlands < requiredNonlands) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (!card.hasType(CardType.LAND)) {
                nonlands++;
            }
        }

        if (!revealed.isEmpty()) {
            GameLog.Builder revealLog = GameLog.builder()
                    .text(gameData.playerIdToName.get(controllerId) + " reveals ");
            for (int i = 0; i < revealed.size(); i++) {
                if (i > 0) {
                    revealLog.text(", ");
                }
                revealLog.card(revealed.get(i));
            }
            gameLogService.append(gameData, revealLog.text(" with ").card(entry.getCard()).text(".").build());
        }

        vividRevealSupport.begin(gameData, controllerId, revealed);
    }
}
