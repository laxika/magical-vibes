package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
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
public class DestroyTargetThenRevealUntilTypeToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetThenRevealUntilTypeToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetThenRevealUntilTypeToBattlefieldEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, entry.getTargetId());
        if (targetControllerId == null) {
            return;
        }

        String targetControllerName = gameData.playerIdToName.get(targetControllerId);
        String targetName = target.getCard().getName();

        // Destroy the targeted permanent (may fail due to indestructible)
        boolean destroyed = permanentRemovalService.tryDestroyPermanent(gameData, target, e.cannotBeRegenerated());
        if (destroyed) {
            String destroyLog = targetName + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, destroyLog);
            log.info("Game {} - {} is destroyed by {}", gameData.id, targetName, entry.getCard().getName());
        }

        // Reveal cards from the top of the controller's library until a matching card is found
        // This happens regardless of whether the destruction succeeded
        List<Card> deck = gameData.playerDecks.get(targetControllerId);
        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;

        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (cardSpecificSupport.cardMatchesAnyType(card, e.cardTypes())) {
                foundCard = card;
                break;
            }
        }

        if (revealedCards.isEmpty()) {
            String emptyLog = targetControllerName + "'s library is empty — no cards are revealed.";
            gameBroadcastService.logAndBroadcast(gameData, emptyLog);
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        String revealLog = targetControllerName + " reveals " + revealedNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        if (foundCard == null) {
            // No matching card found — shuffle all revealed cards back into the library
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);
            String noMatchLog = targetControllerName + " reveals their entire library — no matching card found. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, noMatchLog);
            return;
        }

        // Put the found card onto the battlefield under the controller's control
        Permanent perm = new Permanent(foundCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetControllerId, perm);

        String enterLog = foundCard.getName() + " enters the battlefield under " + targetControllerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, enterLog);

        // Handle ETB effects for creatures
        boolean isCreature = foundCard.hasType(CardType.CREATURE);
        if (isCreature) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetControllerId, foundCard, null, false);
        }

        // Handle planeswalkers
        if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
            perm.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
            perm.setSummoningSick(false);
        }

        // Shuffle all other revealed cards back into the library
        revealedCards.remove(foundCard);
        if (!revealedCards.isEmpty()) {
            deck.addAll(revealedCards);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);

        String shuffleLog = targetControllerName + " shuffles their library.";
        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);

        // Check legend rule
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, targetControllerId);
        }

        log.info("Game {} - {} destroyed {}, {} enters the battlefield",
                gameData.id, targetControllerName, targetName, foundCard.getName());
    }
}
