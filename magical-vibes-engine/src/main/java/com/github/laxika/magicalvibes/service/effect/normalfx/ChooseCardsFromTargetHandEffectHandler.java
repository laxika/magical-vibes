package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reveal the target player's hand, let the caster choose card(s), and route each chosen card to the
 * effect's {@link com.github.laxika.magicalvibes.model.effect.HandChoiceDestination}:
 * <ul>
 *   <li>DISCARD / EXILE reuse {@link PlayerInteractionSupport#resolveHandRevealAndChoose} (type
 *       filtering + "reveals their hand" flow); DISCARD sets {@code discardCausedByOpponent} and
 *       EXILE forwards the source permanent id when {@code returnOnSourceLeave}.</li>
 *   <li>TOP_OF_LIBRARY normally reveals every card ("looks at ... hand") with no type filter and
 *       begins a put-on-top choice. A positive library position enables filtered placement at that
 *       position; the final placement is applied by the RevealedHandChoice answer handler.</li>
 *   <li>SHUFFLE_INTO_LIBRARY uses the public hand-reveal flow and shuffles only after a card is
 *       chosen; an empty hand therefore causes no shuffle.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardsFromTargetHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final CardRevealService cardRevealService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardsFromTargetHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseCardsFromTargetHandEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = amountEvaluationService.evaluate(gameData, e.count(), AmountContext.forStackEntry(entry, source));

        // An X of 0 (e.g. Mind Warp for X=0) chooses no cards: nothing to reveal-and-choose.
        if (count <= 0) {
            return;
        }

        switch (e.destination()) {
            case DISCARD -> {
                gameData.discardCausedByOpponent = true;
                if (e.declineEffect() != null) {
                    playerInteractionSupport.resolveHandRevealAndChooseOrElse(gameData, entry, count,
                            e.excludedTypes(), e.includedTypes(), e.filter(), e.declineEffect(), e);
                } else if (e.revealHand()) {
                    playerInteractionSupport.resolveHandRevealAndChoose(gameData, entry, count,
                            e.excludedTypes(), e.includedTypes(), e.filter(), true, false, null,
                            e.upTo(), false, e.declineFallbackDiscardCount());
                } else {
                    playerInteractionSupport.resolveHandLookAndChoose(gameData, entry, count,
                            e.excludedTypes(), e.includedTypes(), e.filter(), true, false, null,
                            e.upTo(), e.declineFallbackDiscardCount());
                }
            }
            case EXILE -> {
                UUID sourcePermanentId = e.returnOnSourceLeave() || e.imprintOnSource()
                        ? entry.getSourcePermanentId() : null;
                if (e.grantPlayPermission() || e.returnAtNextEndStep()) {
                    playerInteractionSupport.resolveHandRevealAndChoose(gameData, entry, count,
                            e.excludedTypes(), e.includedTypes(), e.filter(), false, true, sourcePermanentId,
                            e.upTo(), e.exileAllCopiesOfChosenNames(), 0, e.imprintOnSource(),
                            e.grantPlayPermission(), e.returnAtNextEndStep(), e.exilePlayOpponentTax());
                } else {
                    playerInteractionSupport.resolveHandRevealAndChoose(gameData, entry, count,
                            e.excludedTypes(), e.includedTypes(), e.filter(), false, true, sourcePermanentId,
                            e.upTo(), e.exileAllCopiesOfChosenNames(), 0, e.imprintOnSource(), e.revealHand());
                }
            }
            case TOP_OF_LIBRARY -> resolveToTopOfLibrary(gameData, entry, count, e);
            case SHUFFLE_INTO_LIBRARY ->
                    playerInteractionSupport.resolveHandRevealAndChooseToShuffleIntoLibrary(gameData, entry, count);
        }
    }

    private void resolveToTopOfLibrary(GameData gameData, StackEntry entry, int count,
                                       ChooseCardsFromTargetHandEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand == null || hand.isEmpty()) {
            if (effect.libraryPosition() > 0 && effect.revealHand()) {
                cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
            } else {
                String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            }
            return;
        }

        if (effect.libraryPosition() > 0 && effect.revealHand()) {
            cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
        } else {
            String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
            String logEntry = casterName + " looks at " + targetName + "'s hand: " + cardNames + ".";
            gameLogService.append(gameData, GameLog.text(logEntry));
        }

        int position = effect.libraryPosition();

        List<Integer> validIndices = new ArrayList<>();
        UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
        for (int i = 0; i < hand.size(); i++) {
            Card handCard = hand.get(i);
            boolean valid = position == 0 || matchesChoiceFilter(gameData, entry, effect, handCard, sourceCardId,
                    targetPlayerId);
            if (valid) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    casterName + " cannot choose a card (" + targetName + "'s hand contains no valid choices)."));
            log.info("Game {} - {}'s hand has no valid library-placement choices for {}",
                    gameData.id, targetName, casterName);
            return;
        }

        int cardsToChoose = Math.min(count, validIndices.size());

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, cardsToChoose, false, false, List.of(), null,
                choicePrompt(effect, targetName, position), false, false, false, null, null, 0,
                effect.filter(), false, false, false, false, false, false, 0, position));

        log.info("Game {} - {} choosing {} card(s) from {}'s hand for library placement at position {}",
                gameData.id, casterName, cardsToChoose, targetName, position);
    }

    private boolean matchesChoiceFilter(GameData gameData, StackEntry entry,
                                         ChooseCardsFromTargetHandEffect effect, Card card,
                                         UUID sourceCardId, UUID targetPlayerId) {
        boolean typeMatches = effect.includedTypes().isEmpty()
                ? !effect.excludedTypes().contains(card.getType())
                : effect.includedTypes().contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(effect.includedTypes()::contains);
        return typeMatches && (effect.filter() == null || predicateEvaluationService.matchesCardPredicate(
                card, effect.filter(), sourceCardId, gameData, targetPlayerId, null, null, entry.getXValue()));
    }

    private String choicePrompt(ChooseCardsFromTargetHandEffect effect, String targetName, int position) {
        if (position == 2 && effect.excludedTypes().contains(CardType.LAND)) {
            return "Choose a nonland card to put third from the top of " + targetName + "'s library.";
        }
        return position == 0
                ? "Choose a card to put on top of " + targetName + "'s library."
                : "Choose a card to put " + ordinal(position) + " from the top of " + targetName + "'s library.";
    }

    private String ordinal(int zeroBasedPosition) {
        return switch (zeroBasedPosition) {
            case 1 -> "second";
            case 2 -> "third";
            default -> (zeroBasedPosition + 1) + "th";
        };
    }
}
