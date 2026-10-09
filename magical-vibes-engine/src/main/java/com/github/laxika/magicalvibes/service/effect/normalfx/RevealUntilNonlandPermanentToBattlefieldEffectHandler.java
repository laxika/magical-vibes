package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.model.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealUntilNonlandPermanentToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandPermanentToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundPermanent = null;
        for (Card card : deck) {
            revealedCards.add(card);
            if (isNonlandPermanent(card)) {
                foundPermanent = card;
                break;
            }
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals "
                        + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + " from the top of their library."));

        boolean canEnter = foundPermanent != null
                && !gameQueryService.isCardBlockedFromEnteringFromZone(gameData, foundPermanent, Zone.LIBRARY)
                && canAttach(gameData, controllerId, foundPermanent);
        if (canEnter) {
            revealedCards.remove(foundPermanent);
        } else if (foundPermanent == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library without finding a nonland permanent card."));
        }

        if (!revealedCards.isEmpty()) {
            deck.removeAll(revealedCards);
            Collections.shuffle(revealedCards);
            deck.addAll(revealedCards);
        }
        if (canEnter) {
            battlefieldEntryBatchSupport.begin(gameData, List.of(new BattlefieldEntryCard(
                    controllerId, controllerId, foundPermanent, Zone.LIBRARY, null)));
        }
    }

    private boolean canAttach(GameData gameData, java.util.UUID controllerId, Card card) {
        if (!card.isAura() || card.isEnchantZone()) {
            return true;
        }
        return gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .anyMatch(host -> !gameQueryService.cantBeEnchantedByOtherAuras(gameData, host)
                        && auraAttachmentService.canEnchant(gameData, card, controllerId, host))
                || gameData.orderedPlayerIds.stream()
                .anyMatch(playerId -> auraAttachmentService.canEnchantPlayer(gameData, card, controllerId, playerId));
    }

    private boolean isNonlandPermanent(Card card) {
        return !card.hasType(CardType.LAND)
                && card.getType() != null
                && card.getType().isPermanentType();
    }
}
