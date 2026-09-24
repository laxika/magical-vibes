package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCreatureCardsToBattlefieldAndShuffleRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Resolves the delayed creature-card reveal and simultaneous battlefield entry. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCreatureCardsToBattlefieldAndShuffleRestEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealCreatureCardsToBattlefieldAndShuffleRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealCreatureCardsToBattlefieldAndShuffleRestEffect) effect;
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (typedEffect.creatureCount() <= 0 || deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        List<Card> creatureCards = new ArrayList<>();
        while (!deck.isEmpty() && creatureCards.size() < typedEffect.creatureCount()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (cardSpecificSupport.cardMatchesAnyType(card, Set.of(CardType.CREATURE))) {
                creatureCards.add(card);
            }
        }

        gameLogService.append(gameData, GameLog.text(
                controllerName + " reveals "
                        + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + "."));

        Set<CardType> enterTappedTypesSnapshot = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Card> enteredCards = new ArrayList<>();
        List<Permanent> enteredPermanents = new ArrayList<>();
        for (Card creatureCard : creatureCards) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, creatureCard, Zone.LIBRARY)) {
                gameLogService.append(gameData, GameLog.cardThen(creatureCard,
                        " can't enter the battlefield from a library; it stays in the library."));
                continue;
            }

            Permanent permanent = new Permanent(creatureCard);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypesSnapshot, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            enteredCards.add(creatureCard);
            enteredPermanents.add(permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(creatureCard, controllerName));
        }

        for (int i = 0; i < enteredCards.size(); i++) {
            Card creatureCard = enteredCards.get(i);
            Permanent permanent = enteredPermanents.get(i);
            battlefieldEntryService.processCreatureETBEffects(
                    gameData, controllerId, creatureCard, null, false);
            if (creatureCard.hasType(CardType.PLANESWALKER) && creatureCard.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, creatureCard.getLoyalty());
                permanent.setSummoningSick(false);
            }
        }

        List<Card> cardsToShuffle = new ArrayList<>(revealedCards);
        cardsToShuffle.removeAll(creatureCards);
        cardsToShuffle.addAll(creatureCards.stream()
                .filter(creatureCard -> !enteredCards.contains(creatureCard))
                .toList());
        if (!cardsToShuffle.isEmpty()) {
            deck.addAll(cardsToShuffle);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        log.info("Game {} - {} revealed {} cards and put {} creature card(s) onto the battlefield",
                gameData.id, controllerName, revealedCards.size(), enteredCards.size());
    }
}
