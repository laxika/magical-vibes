package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves the digital Seek action without opening a player-choice search interaction. */
@Component
@RequiredArgsConstructor
public class SeekLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekLibraryEffect seek = (SeekLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, seek.count(),
                AmountContext.forStackEntry(entry, source)));
        Integer manaValueBound = seek.manaValueBound() == null ? null
                : amountEvaluationService.evaluate(gameData, seek.manaValueBound().amount(),
                AmountContext.forStackEntry(entry, source)) + seek.manaValueBound().offset();
        if (deck == null || deck.isEmpty() || count == 0) {
            if (count > 0 && seek.destination() != LibrarySearchDestination.HAND) {
                logNoMatch(gameData, entry);
            }
            return;
        }

        List<Card> matchingCards = new ArrayList<>(deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId)
                        && (manaValueBound == null || (seek.manaValueBound().exact()
                        ? card.getManaValue() == manaValueBound
                        : card.getManaValue() <= manaValueBound))
                        && (seek.destination() == LibrarySearchDestination.HAND
                        || !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY)))
                .toList());

        int cardsToSeek = Math.min(count, matchingCards.size());
        if (cardsToSeek == 0 && seek.destination() != LibrarySearchDestination.HAND) {
            logNoMatch(gameData, entry);
        }
        for (int i = 0; i < cardsToSeek; i++) {
            Card chosen = matchingCards.remove(ThreadLocalRandom.current().nextInt(matchingCards.size()));
            deck.removeIf(card -> card.getId().equals(chosen.getId()));
            if (seek.destination() == LibrarySearchDestination.HAND) {
                gameData.addCardToHand(controllerId, chosen);
            } else if (seek.destination() == LibrarySearchDestination.BATTLEFIELD
                    || seek.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED) {
                Permanent permanent = new Permanent(chosen, Zone.LIBRARY);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent,
                        battlefieldEntryService.snapshotEnterTappedTypes(gameData));
                if (seek.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED) {
                    permanent.tap();
                }
                gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                        .text("seeks and puts " + chosen.getName() + " onto the battlefield.").build());
            } else {
                throw new IllegalStateException("Unsupported Seek destination: " + seek.destination());
            }
        }
    }

    private void logNoMatch(GameData gameData, StackEntry entry) {
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks but finds no matching card.").build());
    }
}
