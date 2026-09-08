package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Sin Prodder's upkeep trigger and starts the sequential opponent choices.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(controllerName + " reveals ")
                .card(topCard)
                .text(" (mana value " + topCard.getManaValue() + ") from the top of their library ("
                        + sourceName + ").")
                .build());

        List<UUID> opponents = AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                .apnapOpponents(gameData, controllerId);
        if (opponents.isEmpty()) {
            deck.removeFirst();
            gameData.addCardToHand(controllerId, topCard);
            return;
        }

        promptNext(gameData, entry.getCard(), new RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect(
                List.copyOf(opponents),
                controllerId,
                entry.getSourcePermanentId(),
                topCard.getId(),
                topCard.getManaValue()));
        playerInputService.processNextMayAbility(gameData);
        log.info("Game {} - {} reveals the top card and prompts opponents for {}",
                gameData.id, sourceName, sourceName);
    }

    public void promptNext(GameData gameData, Card sourceCard,
            RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        List<Card> deck = gameData.playerDecks.get(effect.abilityControllerId());
        Card topCard = deck == null ? null : deck.stream()
                .filter(card -> card.getId().equals(effect.revealedCardId()))
                .findFirst()
                .orElse(null);
        String cardName = topCard == null ? "the revealed card" : topCard.getName();
        String prompt = sourceCard.getName() + " — Put " + cardName
                + " into its owner's graveyard? If you do, " + sourceCard.getName()
                + " deals " + effect.manaValue() + " damage to you.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                prompt,
                null,
                null,
                effect.sourcePermanentId()));
    }
}
