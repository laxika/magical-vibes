package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomThenRevealUntilTypeToBattlefieldRestToBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetOnBottomThenRevealUntilTypeToBattlefieldRestToBottomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetOnBottomThenRevealUntilTypeToBattlefieldRestToBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (PutTargetOnBottomThenRevealUntilTypeToBattlefieldRestToBottomEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        String targetControllerName = gameData.playerIdToName.get(targetControllerId);
        String targetName = target.getCard().getName();
        permanentRemovalService.removePermanentToLibraryBottom(gameData, target);
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " is put on the bottom of its owner's library."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        List<Card> deck = gameData.playerDecks.get(targetControllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    targetControllerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (cardSpecificSupport.cardMatchesAnyType(card, typedEffect.cardTypes())) {
                foundCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                targetControllerName + " reveals " + revealedNames + " from the top of their library."));

        Permanent enteringPermanent = null;
        if (foundCard != null) {
            enteringPermanent = new Permanent(foundCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetControllerId, enteringPermanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCard, targetControllerName));

            if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
                enteringPermanent.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
                enteringPermanent.setSummoningSick(false);
            }
            revealedCards.remove(foundCard);
        } else {
            gameLogService.append(gameData, GameLog.text(
                    targetControllerName + " reveals their entire library — no matching card was found."));
        }

        if (!revealedCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, targetControllerId, revealedCards);
        }

        if (enteringPermanent != null && foundCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, targetControllerId, foundCard, null, false);
        }

        log.info("Game {} - {} put {} on the bottom and revealed until {} found {}",
                gameData.id, targetControllerName, targetName, typedEffect.cardTypes(),
                foundCard != null ? foundCard.getName() : "no matching card");
    }
}
