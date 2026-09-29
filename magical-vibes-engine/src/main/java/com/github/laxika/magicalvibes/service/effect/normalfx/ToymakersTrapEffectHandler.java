package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ToymakersTrapEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Resolves The Toymaker's Trap's private number choice and guess. */
@Component
@RequiredArgsConstructor
public class ToymakersTrapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final LifeSupport lifeSupport;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ToymakersTrapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<Integer> availableNumbers = availableNumbers(source);
        List<UUID> opponents = opponentsOf(gameData, entry.getControllerId());
        if (availableNumbers.isEmpty() || opponents.isEmpty()) {
            return;
        }

        playerInputService.beginToymakersTrapNumberChoice(
                gameData, entry.getControllerId(), source.getId(), entry.getCard(), availableNumbers);
    }

    public void completeNumberChoice(GameData gameData, ChoiceContext.ToymakersTrapChoice context,
                                     int chosenNumber) {
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null || !availableNumbers(source).contains(chosenNumber)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        source.getToymakersTrapChosenNumbers().add(chosenNumber);
        List<UUID> opponents = opponentsOf(gameData, context.controllerId());
        if (opponents.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        } else if (opponents.size() == 1) {
            beginGuessChoice(gameData, new ChoiceContext.ToymakersTrapChoice(
                    context.controllerId(), context.sourcePermanentId(), context.sourceCard(),
                    opponents.getFirst(), chosenNumber, true), opponents.getFirst());
        } else {
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ToymakersTrapOpponentChoice(
                    context.controllerId(), context.sourcePermanentId(), context.sourceCard(), chosenNumber));
            playerInputService.beginPlayerChoice(gameData, context.controllerId(), opponents,
                    context.sourceCard().getName() + " — Choose an opponent to guess.");
        }
    }

    public void completeOpponentChoice(GameData gameData, UUID chosenOpponentId,
                                       PermanentChoiceContext.ToymakersTrapOpponentChoice context) {
        if (!opponentsOf(gameData, context.controllerId()).contains(chosenOpponentId)) {
            return;
        }
        beginGuessChoice(gameData,
                new ChoiceContext.ToymakersTrapChoice(
                        context.controllerId(), context.sourcePermanentId(), context.sourceCard(),
                        chosenOpponentId, context.chosenNumber(), true),
                chosenOpponentId);
    }

    public void completeGuess(GameData gameData, ChoiceContext.ToymakersTrapChoice context,
                              int guessedNumber) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }

        if (guessedNumber == context.chosenNumber()) {
            sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());
        } else {
            lifeSupport.applyLifeLoss(gameData, context.opponentId(), guessedNumber, context.sourceCard().getName());
            playerInteractionSupport.applyDrawCards(gameData, context.controllerId(), 1);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void beginGuessChoice(GameData gameData, ChoiceContext.ToymakersTrapChoice context,
                                  UUID opponentId) {
        playerInputService.beginToymakersTrapGuessChoice(gameData,
                new ChoiceContext.ToymakersTrapChoice(
                        context.controllerId(), context.sourcePermanentId(), context.sourceCard(),
                        opponentId, context.chosenNumber(), true));
    }

    private List<Integer> availableNumbers(Permanent source) {
        return IntStream.rangeClosed(1, 5)
                .filter(number -> !source.getToymakersTrapChosenNumbers().contains(number))
                .boxed()
                .toList();
    }

    private List<UUID> opponentsOf(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
