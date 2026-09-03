package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealAnyNumberOfCardsFromHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.RevealAnyNumberOfCardsFromHandChoice> {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final InputCompletionService inputCompletionService;
    private final com.github.laxika.magicalvibes.service.ability.AbilityActivationService abilityActivationService;
    private final EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensSupport eachPlayerRevealSupport;

    @Override
    public Class<PendingInteraction.RevealAnyNumberOfCardsFromHandChoice> handledType() {
        return PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.RevealAnyNumberOfCardsFromHandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCardIds == null) {
            chosenCardIds = List.of();
        }
        Set<UUID> uniqueIds = new HashSet<>();
        for (UUID id : chosenCardIds) {
            if (!interaction.validCardIds().contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
            if (!uniqueIds.add(id)) {
                throw new IllegalStateException("Duplicate card ID: " + id);
            }
        }

        List<Card> selectedCards = gameData.playerHands
                .getOrDefault(interaction.playerId(), List.of())
                .stream()
                .filter(card -> uniqueIds.contains(card.getId()))
                .toList();

        PendingInteraction.EachPlayerRevealContext eachPlayerContext = interaction.eachPlayerRevealContext();
        if (eachPlayerContext != null) {
            handleEachPlayerReveal(gameData, interaction, selectedCards, eachPlayerContext);
            return;
        }

        PendingInteraction.ActivatedAbilityRevealContext abilityContext = interaction.activatedAbilityContext();
        if (abilityContext != null) {
            abilityActivationService.handleActivatedAbilityRevealCardsChosen(
                    gameData, player, interaction, selectedCardIds(chosenCardIds));
            return;
        }

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        if (selectedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " reveals no cards."));
        } else {
            GameLog.Builder reveal = GameLog.builder().text(playerName + " reveals ");
            for (int i = 0; i < selectedCards.size(); i++) {
                if (i > 0) {
                    reveal.text(", ");
                }
                reveal.card(selectedCards.get(i));
            }
            gameLogService.append(gameData, reveal.text(".").build());
            cardRevealService.revealToAllPlayers(
                    gameData, interaction.playerId(), GameEventFact.RevealZone.HAND, selectedCards);
        }

        gameData.interaction.clearAwaitingInput();

        PendingInteraction.ManaAbilityRevealContext manaContext = interaction.manaAbilityContext();
        if (manaContext != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, manaContext.sourcePermanentId());
            int amount = amountEvaluationService.evaluate(gameData, manaContext.amount(),
                    new AmountContext(interaction.playerId(), source, null,
                            manaContext.xValue(), selectedCards.size())) * manaContext.manaMultiplier();
            if (amount > 0) {
                ManaPool pool = gameData.playerManaPools.get(interaction.playerId());
                ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData,
                        interaction.playerId(), manaContext.manaColor());
                pool.add(effectiveColor, amount);
                if (manaContext.creatureSource()) {
                    pool.addCreatureMana(effectiveColor, amount);
                }
                GameLog.Builder manaLog = GameLog.builder()
                        .text(playerName + " adds " + amount + " " + manaContext.manaColor().getCode()
                                + " from ");
                if (source != null) {
                    manaLog.card(source.getCard());
                }
                gameLogService.append(gameData, manaLog.text(".").build());
            }
            inputCompletionService.sbaThenAutoPassWithoutResumingParkedResolution(gameData);
            return;
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect resolution is waiting for this choice");
        }
        entry.setEventValue(selectedCards.size());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleEachPlayerReveal(GameData gameData,
                                        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice interaction,
                                        List<Card> selectedCards,
                                        PendingInteraction.EachPlayerRevealContext context) {
        String playerName = gameData.playerIdToName.get(interaction.playerId());
        if (selectedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " reveals no cards."));
        } else {
            GameLog.Builder reveal = GameLog.builder().text(playerName + " reveals ");
            for (int i = 0; i < selectedCards.size(); i++) {
                if (i > 0) {
                    reveal.text(", ");
                }
                reveal.card(selectedCards.get(i));
            }
            gameLogService.append(gameData, reveal.text(".").build());
            cardRevealService.revealToAllPlayers(
                    gameData, interaction.playerId(), GameEventFact.RevealZone.HAND, selectedCards);
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect resolution is waiting for this choice");
        }
        Map<UUID, Integer> revealedCounts = new LinkedHashMap<>(context.revealedCounts());
        revealedCounts.put(interaction.playerId(), selectedCards.size());
        EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect effect =
                new EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect(
                        context.filter(), context.token());

        gameData.interaction.clearAwaitingInput();
        boolean begunNext = eachPlayerRevealSupport.beginNextChoice(
                gameData, entry, context.remainingPlayerIds(), context.playerOrder(), revealedCounts, effect);
        inputCompletionService.publishStateAfterInput(gameData);
        if (!begunNext) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    private List<UUID> selectedCardIds(List<UUID> chosenCardIds) {
        return List.copyOf(chosenCardIds);
    }
}
