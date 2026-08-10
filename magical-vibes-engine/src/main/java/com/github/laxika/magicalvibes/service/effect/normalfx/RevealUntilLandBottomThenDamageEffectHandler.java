package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandBottomThenDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealUntilLandBottomThenDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilLandBottomThenDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> revealed = new ArrayList<>();
        boolean mountainRevealed = false;

        while (deck != null && !deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.LAND)) {
                mountainRevealed = card.getSubtypes().contains(CardSubtype.MOUNTAIN);
                break;
            }
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        if (revealed.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        String revealedNames = revealed.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + revealedNames
                + " from the top of their library with " + entry.getCard().getName() + "."));

        int damage = (int) revealed.stream().filter(card -> !card.hasType(CardType.LAND)).count();
        if (mountainRevealed) {
            damage *= 2;
        }
        if (damage > 0 && entry.getTargetId() != null) {
            damageSupport.resolveAnyTargetDamage(gameData, entry, entry.getTargetId(), damage, false);
            gameOutcomeService.checkWinCondition(gameData);
        }

        libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, revealed);
    }
}
