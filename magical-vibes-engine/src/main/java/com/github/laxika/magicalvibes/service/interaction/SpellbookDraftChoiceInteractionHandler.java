package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DraftCardFromSpellbookEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpellbookDraftChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SpellbookDraftChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final CardRevealService cardRevealService;
    private final ExileService exileService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DraftCardFromSpellbookEffectHandler draftHandler;
    private final LibraryChoiceHandlerService libraryChoiceHandlerService;

    @Override
    public Class<PendingInteraction.SpellbookDraftChoice> handledType() {
        return PendingInteraction.SpellbookDraftChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SpellbookDraftChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to draft");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalStateException("Choose exactly one spellbook card");
        }

        Card chosen = interaction.cards().stream()
                .filter(card -> card.getId().equals(cardIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid spellbook card"));

        gameData.interaction.clearAwaitingInput();
        StackEntry drafting = gameData.pendingEffectResolutionEntry;
        StepOutcome drafted;
        if (interaction.putChosenCardOntoBattlefield()) {
            Permanent permanent = new Permanent(chosen);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, interaction.playerId(), permanent);
            // Every permanent's entry abilities, not only a creature's (an Equipment's, a Vehicle's).
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, interaction.playerId(), chosen, null, false);
            drafted = StepOutcome.ofObjects(List.of(permanent.getId()), List.of(interaction.playerId()));
            if (interaction.draft() != null && interaction.draft().battlefieldSelectionFollowUp() != null
                    && gameData.playerBattlefields.getOrDefault(interaction.playerId(), List.of()).contains(permanent)) {
                // "When an Equipment enters this way, attach it to target creature you control."
                libraryChoiceHandlerService.queueLibrarySelectionFollowUp(gameData,
                        interaction.draft().battlefieldSelectionFollowUp(), List.of(permanent.getId()));
            }
        } else if (interaction.exileChosenCard()) {
            exileService.exileCard(gameData, interaction.playerId(), chosen);
            gameData.grantExilePlayPermission(chosen.getId(), interaction.playerId(), Duration.UNTIL_END_OF_TURN);
            drafted = draftedInto(chosen, Zone.EXILE, interaction.playerId());
        } else {
            gameData.addCardToHand(interaction.playerId(), chosen);
            drafted = draftedInto(chosen, Zone.HAND, interaction.playerId());
        }
        if (drafting != null) {
            // The draft step's result, which DraftCardFromSpellbookEffectHandler began as nothing (StepKind.CONJURE).
            drafting.addToStep(StepKind.CONJURE, drafted);
        }
        if (interaction.revealChosenCard()) {
            cardRevealService.revealToAllPlayers(gameData, interaction.playerId(),
                    GameEventFact.RevealZone.HAND, List.of(chosen));
        }
        if (!interaction.chosenCardEffects().isEmpty()
                && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.setChosenObjectCard(chosen);
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, interaction.chosenCardEffects());
        }
        // A card drafted into a hand, or held, is hidden (CR 400.2): the draft was announced as "a card", and only a
        // card that goes public or is revealed is named.
        if (interaction.putChosenCardOntoBattlefield() || interaction.exileChosenCard()
                || interaction.revealChosenCard()) {
            String destination = interaction.putChosenCardOntoBattlefield()
                    ? " onto the battlefield" : interaction.exileChosenCard() ? " into exile" : "";
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(interaction.playerId())
                            + (interaction.conjure() ? " conjures " : " drafts "), chosen,
                    " from " + interaction.sourceCardName() + "'s spellbook" + destination + "."));
        }
        if (interaction.conjure()) {
            ActionEvents.conjured(gameData, interaction.playerId(), 1);
        }
        if (interaction.draftsLeft() > 0 && draftHandler.offer(gameData, interaction.draft(), interaction.playerId(),
                drafting != null ? drafting.getControllerId() : interaction.playerId(), interaction.sourceCardName(),
                interaction.draftsLeft() - 1)) {
            // "Draft a card ... twice": the next draft is part of the same step.
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private static StepOutcome draftedInto(Card card, Zone zone, UUID ownerId) {
        return StepOutcome.ofObjects(List.of(card.getId()), List.of(ownerId),
                List.of(new MovedCard(card.getId(), card.getId(), zone, ownerId)));
    }
}
