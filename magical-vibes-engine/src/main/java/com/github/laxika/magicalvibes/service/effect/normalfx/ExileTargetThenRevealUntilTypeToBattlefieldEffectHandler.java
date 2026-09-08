package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetThenRevealUntilTypeToBattlefieldEffect;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetThenRevealUntilTypeToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetThenRevealUntilTypeToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (ExileTargetThenRevealUntilTypeToBattlefieldEffect) effect;
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
        permanentRemovalService.removePermanentToExile(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));

        List<Card> deck = gameData.playerDecks.get(targetControllerId);
        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (deck != null && !deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (cardSpecificSupport.cardMatchesAnyType(card, typedEffect.cardTypes())) {
                foundCard = card;
                break;
            }
        }

        if (revealedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    targetControllerName + "'s library is empty — no cards are revealed."));
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(targetControllerName + " reveals " + revealedNames + "."));

        if (foundCard == null) {
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);
            gameLogService.append(gameData, GameLog.text(
                    targetControllerName + " reveals their entire library — no matching card found. Library is shuffled."));
            return;
        }

        Permanent enteringPermanent = new Permanent(foundCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetControllerId, enteringPermanent);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCard, targetControllerName));

        if (foundCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, targetControllerId, foundCard, null, false);
        }
        if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
            enteringPermanent.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
            enteringPermanent.setSummoningSick(false);
        }

        revealedCards.remove(foundCard);
        if (!revealedCards.isEmpty()) {
            deck.addAll(revealedCards);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);
        gameLogService.append(gameData, GameLog.text(targetControllerName + " shuffles their library."));

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, targetControllerId);
        }

        log.info("Game {} - {} exiled {}, {} enters the battlefield",
                gameData.id, targetControllerName, targetName, foundCard.getName());
    }
}
