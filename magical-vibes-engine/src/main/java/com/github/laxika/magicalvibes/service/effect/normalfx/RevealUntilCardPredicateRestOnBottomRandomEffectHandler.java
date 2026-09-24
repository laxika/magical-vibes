package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilCardPredicateRestOnBottomRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AttackLegalityService attackLegalityService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCardPredicateRestOnBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolve(gameData, entry, (RevealUntilCardPredicateRestOnBottomRandomEffect) effect, false);
    }

    void resolveUsingSourcePermanentSnapshot(GameData gameData, StackEntry entry, CardEffect effect,
                                             Permanent sourcePermanentSnapshot) {
        StackEntry snapshotEntry = new StackEntry(entry);
        snapshotEntry.setSourcePermanentSnapshot(sourcePermanentSnapshot);
        resolve(gameData, snapshotEntry, effect);
    }

    void resolve(GameData gameData, StackEntry entry,
                 RevealUntilCardPredicateRestOnBottomRandomEffect typedEffect, boolean shuffleLibrary) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            if (shuffleLibrary && deck != null) LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
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
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId,
                    entry.getSourcePermanentId(), null, entry.getXValue(), entry.getSourcePermanentSnapshot())) {
                foundCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library."));

        boolean toBattlefield = typedEffect.destination() == LibrarySearchDestination.BATTLEFIELD;
        Permanent permanent = null;
        boolean entryBlocked = foundCard != null && toBattlefield
                && gameQueryService.isCardBlockedFromEnteringFromZone(gameData, foundCard, Zone.LIBRARY);
        if (entryBlocked) {
            gameLogService.append(gameData, GameLog.cardThen(foundCard,
                    " can't enter the battlefield from a library; it stays in the library."));
            revealedCards.remove(foundCard);
            deck.addFirst(foundCard);
            foundCard = null;
        }
        if (foundCard != null) {
            revealedCards.remove(foundCard);
            if (toBattlefield && typedEffect.enterTappedAndAttacking()) {
                beginAttackTargetChoice(gameData, new PermanentChoiceContext.RevealUntilCardPredicateAttackTarget(
                        entry.getCard(), controllerId, foundCard, revealedCards));
                return;
            }

            if (toBattlefield) {
                permanent = new Permanent(foundCard, Zone.LIBRARY);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
                gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCard, playerName));

                if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
                    permanent.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
                    permanent.setSummoningSick(false);
                }
            } else {
                gameData.addCardToHand(controllerId, foundCard);
                gameLogService.append(gameData, GameLog.text(
                        playerName + " puts " + foundCard.getName() + " into their hand."));
            }

        } else if (!entryBlocked) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no matching card was found."));
        }

        if (shuffleLibrary) {
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        } else if (!revealedCards.isEmpty()) {
            Collections.shuffle(revealedCards);
            deck.addAll(revealedCards);
        }

        if (permanent != null && foundCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, foundCard, null, false);
        }
        if (permanent != null && !gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private void beginAttackTargetChoice(
            GameData gameData, PermanentChoiceContext.RevealUntilCardPredicateAttackTarget context) {
        var validTargetIds = attackLegalityService.getValidAttackTargetIds(gameData, context.controllerId());
        List<UUID> validPlayerIds = gameData.orderedPlayerIds.stream()
                .filter(validTargetIds::contains)
                .toList();
        List<UUID> validPermanentIds = gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream())
                .filter(permanent -> validTargetIds.contains(permanent.getId()))
                .map(Permanent::getId)
                .toList();

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginAnyTargetChoice(
                gameData, context.controllerId(), validPermanentIds, validPlayerIds,
                "Choose the player, planeswalker, or battle for "
                        + context.foundCard().getName() + " to attack.");
    }

    public void completeAttackTargetChoice(
            GameData gameData, UUID attackTargetId,
            PermanentChoiceContext.RevealUntilCardPredicateAttackTarget context) {
        List<Card> deck = gameData.playerDecks.get(context.controllerId());
        if (deck != null && !context.remainingRevealedCards().isEmpty()) {
            List<Card> remainingRevealedCards = new ArrayList<>(context.remainingRevealedCards());
            Collections.shuffle(remainingRevealedCards);
            deck.addAll(remainingRevealedCards);
        }

        Permanent permanent = new Permanent(context.foundCard());
        permanent.tap();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, context.controllerId(), permanent);
        if (context.foundCard().hasType(CardType.CREATURE)) {
            permanent.setAttacking(true);
            permanent.setAttackTarget(attackTargetId);
        }
        gameLogService.append(gameData,
                GameLog.cardThen(context.foundCard(), " enters the battlefield tapped and attacking."));

        if (context.foundCard().hasType(CardType.PLANESWALKER)
                && context.foundCard().getLoyalty() != null) {
            permanent.setCounterCount(CounterType.LOYALTY, context.foundCard().getLoyalty());
            permanent.setSummoningSick(false);
        }
        if (context.foundCard().hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, context.controllerId(), context.foundCard(), null, false);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, context.controllerId());
        }
    }
}
