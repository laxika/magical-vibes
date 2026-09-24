package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenCardAwareEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
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
            gameData.exilePlayPermissions.put(chosen.getId(), interaction.playerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(chosen.getId());
        } else {
            gameData.addCardToHand(interaction.playerId(), chosen);
        }
        if (interaction.revealChosenCard()) {
            cardRevealService.revealToAllPlayers(gameData, interaction.playerId(),
                    GameEventFact.RevealZone.HAND, List.of(chosen));
        }
        if (!interaction.chosenCardEffects().isEmpty()
                && gameData.pendingEffectResolutionEntry != null) {
            List<CardEffect> followUpEffects = interaction.chosenCardEffects().stream()
                    .map(effect -> effect instanceof ChosenCardAwareEffect chosenCardAwareEffect
                            ? chosenCardAwareEffect.withChosenCard(chosen) : effect)
                    .toList();
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, followUpEffects);
        }
        String destination = interaction.putChosenCardOntoBattlefield()
                ? " onto the battlefield" : interaction.exileChosenCard() ? " into exile" : "";
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(interaction.playerId()) + " drafts ", chosen,
                " from " + interaction.sourceCardName() + "'s spellbook" + destination + "."));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
