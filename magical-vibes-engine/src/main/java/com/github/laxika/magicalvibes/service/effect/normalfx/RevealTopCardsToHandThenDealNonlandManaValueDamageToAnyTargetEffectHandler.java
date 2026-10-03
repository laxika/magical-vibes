package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsToHandThenDealNonlandManaValueDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardsToHandThenDealNonlandManaValueDamageToAnyTargetEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsToHandThenDealNonlandManaValueDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, 2);
        if (result == null) {
            return;
        }

        List<Card> revealed = result.topCards();
        GameLog.Builder revealBuilder = GameLog.builder()
                .text(result.playerName() + " reveals ");
        for (int i = 0; i < revealed.size(); i++) {
            if (i > 0) {
                revealBuilder.text(", ");
            }
            revealBuilder.card(revealed.get(i));
        }
        gameLogService.append(gameData, revealBuilder
                .text(" from the top of their library with ")
                .card(entry.getCard())
                .text(".")
                .build());

        int totalManaValue = revealed.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .mapToInt(Card::getManaValue)
                .sum();

        for (Card card : revealed) {
            gameData.addCardToHand(result.controllerId(), card);
        }

        if (totalManaValue <= 0 || entry.getTargetId() == null) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, totalManaValue, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, entry.getTargetId(), damage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
