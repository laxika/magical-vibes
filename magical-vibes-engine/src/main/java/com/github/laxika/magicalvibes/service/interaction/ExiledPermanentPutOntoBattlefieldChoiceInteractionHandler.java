package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTopCardMayPutPermanentSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Primal Surge's "you may put it onto the battlefield" prompt for the permanent card just exiled
 * from the top of the controller's library. Accepting puts it onto the battlefield and repeats the
 * process (exile the next card, prompt again if it is a permanent card); declining ends the
 * resolution and leaves the card in exile.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExiledPermanentPutOntoBattlefieldChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice> {

    private final ExileTopCardMayPutPermanentSupport support;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final AuraAttachmentService auraAttachmentService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    @Override
    public Class<PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice> handledType() {
        return PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your " + interaction.sourceName() + " choice");
        }

        boolean accepted = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        UUID controllerId = interaction.playerId();
        String sourceName = interaction.sourceName();
        String playerName = gameData.playerIdToName.get(controllerId);

        gameData.interaction.clearAwaitingInput();

        if (accepted) {
            ExiledCardEntry entry = gameData.findExiledCard(interaction.cardId());
            if (entry != null && entry.card().isAura() && !hasLegalEnchantObject(gameData, controllerId, entry.card())) {
                gameLogService.append(gameData, GameLog.text(interaction.cardName()
                        + " can't enchant anything and stays in exile (" + sourceName + ")."));
            } else if (entry != null && entry.card().isAura() && !entry.card().isEnchantZone()) {
                // CR 303.4f: the Aura's controller chooses what it enchants as it enters; the batch asks for
                // the host when there is more than one and then places the Aura.
                battlefieldEntryBatchSupport.begin(gameData, List.of(new com.github.laxika.magicalvibes.model
                        .BattlefieldEntryCard(controllerId, controllerId, entry.card(),
                        com.github.laxika.magicalvibes.model.Zone.EXILE, null)));
                log.info("Game {} - {} puts {} onto the battlefield via {}",
                        gameData.id, playerName, entry.card().getName(), sourceName);
                if (support.exileTopCardAndPromptIfPermanent(gameData, controllerId, sourceName)) {
                    return;
                }
            } else {
                if (entry != null) {
                    Card card = entry.card();
                    gameData.removeFromExile(card.getId());
                    graveyardReturnSupport.putCardOntoBattlefieldFromExile(gameData, controllerId, card);
                    log.info("Game {} - {} puts {} onto the battlefield via {}",
                            gameData.id, playerName, card.getName(), sourceName);
                }
                if (support.exileTopCardAndPromptIfPermanent(gameData, controllerId, sourceName)) {
                    return;
                }
            }
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " leaves " + interaction.cardName()
                    + " in exile (" + sourceName + ")."));
        }

        // Resumes any remaining effects/triggers and auto-passes. The shared epilogue matters here:
        // the permanents put onto the battlefield can have queued ETB may-abilities.
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Whether an Aura put onto the battlefield by {@code controllerId} has any legal object or player to
     * enchant (CR 303.4f). An Aura with none stays in exile instead of entering (CR 303.4g).
     */
    private boolean hasLegalEnchantObject(GameData gameData, UUID controllerId, Card auraCard) {
        for (UUID battlefieldPlayerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(battlefieldPlayerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (auraAttachmentService.canEnchant(gameData, auraCard, controllerId, permanent)) {
                    return true;
                }
            }
        }
        return auraCard.isEnchantPlayer() && gameData.orderedPlayerIds.stream()
                .anyMatch(playerId -> auraAttachmentService.canEnchantPlayer(gameData, auraCard, controllerId, playerId));
    }
}
