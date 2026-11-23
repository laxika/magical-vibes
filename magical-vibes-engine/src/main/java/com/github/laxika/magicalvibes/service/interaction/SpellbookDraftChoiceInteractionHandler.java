package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HoldDraftedSpellbookCardEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
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
        if (interaction.putChosenCardOntoBattlefield()) {
            Permanent permanent = new Permanent(chosen);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, interaction.playerId(), permanent);
            if (chosen.hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, interaction.playerId(), chosen, null, false);
            }
        } else if (interaction.exileChosenCard()) {
            exileService.exileCard(gameData, interaction.playerId(), chosen);
            gameData.grantExilePlayPermission(chosen.getId(), interaction.playerId(), Duration.UNTIL_END_OF_TURN);
        } else if (interaction.chosenCardEffects().stream().noneMatch(HoldDraftedSpellbookCardEffect.class::isInstance)) {
            gameData.addCardToHand(interaction.playerId(), chosen);
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
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
