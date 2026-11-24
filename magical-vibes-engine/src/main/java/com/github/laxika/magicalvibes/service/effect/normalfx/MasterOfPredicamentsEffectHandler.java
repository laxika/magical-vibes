package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MasterOfPredicamentsEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Resolves {@link MasterOfPredicamentsEffect}: the controller chooses a card in hand, then the
 * guesser guesses about it; the guess is answered by {@code MasterOfPredicamentsGuessChoice}, which
 * offers the free cast on a wrong guess and otherwise resolves {@code ifNoSpellCast}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MasterOfPredicamentsEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MasterOfPredicamentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MasterOfPredicamentsEffect guess = (MasterOfPredicamentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID guessingPlayerId = subjectResolutionService.players(gameData, entry, effect, guess.guesser()).stream()
                .findFirst()
                .orElse(null);
        if (guessingPlayerId == null) {
            return;
        }
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            if (guess.ifNoSpellCast() != null) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(guess.ifNoSpellCast()));
            }
            return;
        }

        int threshold = amountEvaluationService.evaluate(gameData, guess.threshold(),
                AmountContext.forStackEntry(entry, null));
        List<Integer> validIndices = IntStream.range(0, hand.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MasterOfPredicamentsCardChoice(
                controllerId, validIndices, "Choose a card in your hand.", guessingPlayerId, entry.getCard(),
                threshold, guess.ifNoSpellCast()));
        log.info("Game {} - Awaiting {} to choose a card for {}",
                gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
    }
}
