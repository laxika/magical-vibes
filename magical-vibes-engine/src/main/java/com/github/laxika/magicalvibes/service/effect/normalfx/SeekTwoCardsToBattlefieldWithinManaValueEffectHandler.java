package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekTwoCardsToBattlefieldWithinManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves a dynamic-mana-value variant of the Inquisitor Captain-style seek. */
@Component
@RequiredArgsConstructor
public class SeekTwoCardsToBattlefieldWithinManaValueEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekTwoCardsToBattlefieldWithinManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekTwoCardsToBattlefieldWithinManaValueEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int maxManaValue = Math.max(0, amountEvaluationService.evaluate(gameData, seek.maxManaValue(),
                AmountContext.forStackEntry(entry, source)));
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> !card.isToken())
                .filter(card -> card.getManaValue() <= maxManaValue)
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        List<Card> soughtCards = new ArrayList<>(matchingCards);
        Collections.shuffle(soughtCards);
        soughtCards = new ArrayList<>(soughtCards.subList(0, Math.min(2, soughtCards.size())));
        soughtCards.forEach(card -> deck.removeIf(libraryCard -> libraryCard.getId().equals(card.getId())));

        if (soughtCards.size() == 1) {
            putOntoBattlefield(gameData, controllerId, soughtCards.getFirst());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId,
                soughtCards,
                soughtCards.stream().map(Card::getId).toList(),
                false,
                false,
                false,
                false,
                false,
                0,
                null,
                1,
                "Choose one of the sought creature cards to put onto the battlefield. "
                        + "Shuffle the other into your library.",
                false,
                1,
                false));
    }

    private void putOntoBattlefield(GameData gameData, UUID controllerId, Card card) {
        String playerName = gameData.playerIdToName.get(controllerId);
        Permanent permanent = new Permanent(card, Zone.LIBRARY);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, playerName));
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, null, false);
    }
}
