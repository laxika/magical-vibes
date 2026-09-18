package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                foundCard = card;
                break;
            }
        }

        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals "
                        + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + " from the top of their library."));

        if (foundCard == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no matching card was found."));
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        revealedCards.remove(foundCard);
        // Keep the found card on top while the may choice is pending. The other revealed cards are
        // already back in the library; the completion path shuffles the whole library either way.
        deck.addFirst(foundCard);
        deck.addAll(revealedCards);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(typedEffect),
                entry.getCard().getName() + " — Put " + foundCard.getName() + " onto the battlefield?",
                foundCard.getId()));
    }

    public void completeMayChoice(GameData gameData, Player player, boolean accepted,
                                  PendingMayAbility ability) {
        UUID controllerId = ability.controllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Card foundCard = null;
        if (deck != null) {
            int foundIndex = -1;
            for (int i = 0; i < deck.size(); i++) {
                if (deck.get(i).getId().equals(ability.targetCardId())) {
                    foundIndex = i;
                    break;
                }
            }
            if (foundIndex >= 0) {
                foundCard = deck.remove(foundIndex);
            }
        }

        if (foundCard != null && accepted) {
            Permanent permanent = new Permanent(foundCard, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(
                    foundCard, gameData.playerIdToName.get(controllerId)));
            if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
                permanent.setSummoningSick(false);
            }
            if (foundCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.processCreatureETBEffects(
                        gameData, controllerId, foundCard, null, false);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }
        } else if (foundCard != null) {
            deck.add(foundCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to put ", foundCard, " onto the battlefield."));
        }

        if (deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
