package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeTargetThenRevealUntilTypeToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetThenRevealUntilTypeToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeTargetThenRevealUntilTypeToBattlefieldEffect) effect;
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

        // Reweave mode: an empty type set means "shares a card type with the sacrificed permanent"
        Set<CardType> matchTypes = e.cardTypes().isEmpty()
                ? permanentTypesOf(target.getCard())
                : e.cardTypes();
        boolean permanentCardsOnly = e.cardTypes().isEmpty();

        // Sacrifice the targeted permanent
        permanentRemovalService.removePermanentToGraveyard(gameData, target);
        String sacrificeLog = targetControllerName + " sacrifices " + targetName + ".";
        gameLogService.append(gameData, GameLog.text(sacrificeLog));

        // Reveal cards from the top of the controller's library until a matching card is found
        List<Card> deck = gameData.playerDecks.get(targetControllerId);
        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;

        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            boolean eligible = !permanentCardsOnly || !permanentTypesOf(card).isEmpty();
            if (eligible && !matchTypes.isEmpty() && cardSpecificSupport.cardMatchesAnyType(card, matchTypes)) {
                foundCard = card;
                break;
            }
        }

        if (revealedCards.isEmpty()) {
            String emptyLog = targetControllerName + "'s library is empty — no cards are revealed.";
            gameLogService.append(gameData, GameLog.text(emptyLog));
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        String revealLog = targetControllerName + " reveals " + revealedNames + ".";
        gameLogService.append(gameData, GameLog.text(revealLog));

        if (foundCard == null) {
            // No matching card found — shuffle all revealed cards back into the library
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);
            String noMatchLog = targetControllerName + " reveals their entire library — no matching card found. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(noMatchLog));
            return;
        }

        // Put the found card onto the battlefield under the controller's control
        Permanent perm = new Permanent(foundCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetControllerId, perm);

        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCard, targetControllerName));

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
        gameLogService.append(gameData, GameLog.text(shuffleLog));

        // Check legend rule
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, targetControllerId);
        }

        log.info("Game {} - {} sacrificed {}, {} enters the battlefield",
                gameData.id, targetControllerName, targetName, foundCard.getName());
    }

    /**
     * The card types of {@code card} that make it a permanent card. Kindred is excluded because it
     * never stands alone as a permanent type.
     */
    private Set<CardType> permanentTypesOf(Card card) {
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        if (card.getType() != null) {
            types.add(card.getType());
        }
        types.addAll(card.getAdditionalTypes());
        types.removeIf(type -> !type.isPermanentType() || type == CardType.KINDRED);
        return types;
    }
}
