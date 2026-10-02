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
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Set<CardType> targetTypes = permanentTypesOf(gameQueryService.getEffectiveCardTypes(gameData, target));
        String targetName = target.getCard().getName();
        permanentRemovalService.removePermanentToLibraryBottom(gameData, target);
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " is put on the bottom of its owner's library."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        var controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (!permanentTypesOf(card).isEmpty()
                    && cardSpecificSupport.cardMatchesAnyType(card, targetTypes)) {
                foundCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                controllerName + " reveals " + revealedNames + " from the top of their library."));

        boolean entryBlocked = foundCard != null
                && gameQueryService.isCardBlockedFromEnteringFromZone(gameData, foundCard, Zone.LIBRARY);
        if (entryBlocked) {
            gameLogService.append(gameData, GameLog.cardThen(foundCard,
                    " can't enter the battlefield from a library; it stays in the library."));
            revealedCards.remove(foundCard);
            deck.addFirst(foundCard);
            foundCard = null;
        }

        if (foundCard == null) {
            if (!entryBlocked) {
                gameLogService.append(gameData, GameLog.text(
                        controllerName + " reveals their entire library — no matching card was found."));
            }
        } else {
            revealedCards.remove(foundCard);
            Permanent enteringPermanent = new Permanent(foundCard, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, enteringPermanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCard, controllerName));

            if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
                enteringPermanent.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
                enteringPermanent.setSummoningSick(false);
            }
            if (foundCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, controllerId, foundCard, null, false);
            }
        }

        if (!revealedCards.isEmpty()) {
            Collections.shuffle(revealedCards);
            deck.addAll(revealedCards);
        }

        log.info("Game {} - {} put {} on the bottom and revealed until a shared card type was found: {}",
                gameData.id, controllerName, targetName,
                foundCard == null ? "none" : foundCard.getName());
    }

    private Set<CardType> permanentTypesOf(Set<CardType> cardTypes) {
        EnumSet<CardType> permanentTypes = EnumSet.noneOf(CardType.class);
        cardTypes.stream()
                .filter(CardType::isPermanentType)
                .forEach(permanentTypes::add);
        return permanentTypes;
    }

    private Set<CardType> permanentTypesOf(Card card) {
        EnumSet<CardType> permanentTypes = EnumSet.noneOf(CardType.class);
        if (card.getType() != null && card.getType().isPermanentType()) {
            permanentTypes.add(card.getType());
        }
        card.getAdditionalTypes().stream()
                .filter(CardType::isPermanentType)
                .forEach(permanentTypes::add);
        return permanentTypes;
    }
}
