package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LimDulsVaultSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Lim-Dûl's Vault's "in any order" prompt. After an accepted repeat the ordered cards go on the
 * bottom of the library and the next look begins; on the final (declined) iteration the library
 * is shuffled first and the ordered cards go on top, ending the resolution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimDulsVaultOrderChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.LimDulsVaultOrderChoice> {

    private final GameLogService gameLogService;
    private final LimDulsVaultSupport limDulsVaultSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.LimDulsVaultOrderChoice> handledType() {
        return PendingInteraction.LimDulsVaultOrderChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardOrder.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.LimDulsVaultOrderChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your Lim-Dûl's Vault choice");
        }

        List<Card> cards = interaction.cards();
        List<Integer> cardOrder = ((InteractionAnswer.CardOrder) answer).cardOrder();
        int count = cards.size();
        if (cardOrder.size() != count) {
            throw new IllegalStateException("Must specify order for all " + count + " cards");
        }
        Set<Integer> seen = new HashSet<>();
        for (int index : cardOrder) {
            if (index < 0 || index >= count) {
                throw new IllegalStateException("Invalid card index: " + index);
            }
            if (!seen.add(index)) {
                throw new IllegalStateException("Duplicate card index: " + index);
            }
        }

        UUID controllerId = interaction.playerId();
        gameData.interaction.clearAwaitingInput();

        // The shuffle happens before the last cards looked at go back, so they stay on top.
        if (!interaction.toBottom()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(player.getUsername() + " shuffles their library."));
        }

        List<Card> library = gameData.playerDecks.get(controllerId);
        for (int i = 0; i < count; i++) {
            Card card = cards.get(cardOrder.get(i));
            if (interaction.toBottom()) {
                library.add(card);
            } else {
                library.add(i, card);
            }
        }

        gameLogService.append(gameData, GameLog.text(player.getUsername() + " puts " + count + " cards on the "
                + (interaction.toBottom() ? "bottom" : "top") + " of their library (Lim-Dûl's Vault)."));
        log.info("Game {} - {} ordered {} Lim-Dûl's Vault cards to the {}", gameData.id, player.getUsername(),
                count, interaction.toBottom() ? "bottom" : "top");

        if (interaction.toBottom() && limDulsVaultSupport.beginLook(gameData, controllerId)) {
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
