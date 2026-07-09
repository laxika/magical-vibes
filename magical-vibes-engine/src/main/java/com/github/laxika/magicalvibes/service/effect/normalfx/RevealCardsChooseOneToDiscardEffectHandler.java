package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToDiscardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RevealCardsChooseOneToDiscardEffect} (Thieving Sprite): the target reveals X cards
 * from their hand (X = number of the caster's permanents matching the effect's {@code countFilter}),
 * then the caster chooses one of those cards for the target to discard.
 *
 * <p>When the target's hand is already ≤ X, every card is revealed and the caster's discard pick begins
 * immediately ({@link PendingInteraction.ChooseRevealedCardToDiscardChoice}). Otherwise the target first
 * picks which X cards to reveal ({@link PendingInteraction.RevealCardsFromHandChoice}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCardsChooseOneToDiscardEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealCardsChooseOneToDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealCardsChooseOneToDiscardEffect) effect;

        UUID casterId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        String casterName = gameData.playerIdToName.get(casterId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        int revealCount = countMatchingPermanents(gameData, casterId, e);

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    targetName + " has no cards in hand to reveal.");
            log.info("Game {} - {}'s hand is empty for reveal-and-discard", gameData.id, targetName);
            return;
        }

        if (revealCount <= 0) {
            gameBroadcastService.logAndBroadcast(gameData,
                    casterName + " controls no matching permanents — no cards are revealed.");
            log.info("Game {} - {} controls 0 matching permanents; nothing revealed", gameData.id, casterName);
            return;
        }

        // Target's hand is already small enough — everything is revealed, go straight to the caster's pick.
        if (hand.size() <= revealCount) {
            String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
            gameBroadcastService.logAndBroadcast(gameData,
                    targetName + " reveals their hand: " + cardNames + ".");
            beginDiscardChoice(gameData, casterId, targetPlayerId, new ArrayList<>(hand));
            return;
        }

        // Target chooses which revealCount cards to reveal.
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsFromHandChoice(
                targetPlayerId, casterId, validIndices, revealCount, new ArrayList<>(),
                "Choose a card to reveal (" + revealCount + " to reveal)."));

        log.info("Game {} - {} must reveal {} cards for {}", gameData.id, targetName, revealCount, casterName);
    }

    private int countMatchingPermanents(GameData gameData, UUID playerId, RevealCardsChooseOneToDiscardEffect e) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.countFilter())) {
                count++;
            }
        }
        return count;
    }

    private void beginDiscardChoice(GameData gameData, UUID casterId, UUID targetPlayerId, List<Card> revealedCards) {
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ChooseRevealedCardToDiscardChoice(
                casterId, targetPlayerId, revealedCards,
                "Choose a card for " + targetName + " to discard."));
    }
}
