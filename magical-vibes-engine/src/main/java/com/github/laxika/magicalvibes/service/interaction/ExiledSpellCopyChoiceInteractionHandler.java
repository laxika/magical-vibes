package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the chosen instant or sorcery card of {@code ExiledSpellCopyChoice}: it is copied
 * {@code copies} times and the copies are queued for a free cast (Chandra, Pyromaster's ultimate).
 * Each copy is an independent card in exile, so the queue can cast them one at a time and pause
 * for each one's targets.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExiledSpellCopyChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExiledSpellCopyChoice> {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<PendingInteraction.ExiledSpellCopyChoice> handledType() {
        return PendingInteraction.ExiledSpellCopyChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExiledSpellCopyChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        UUID chosenId = chosen.isEmpty() ? null : chosen.getFirst();
        if (chosenId != null && !interaction.validCardIds().contains(chosenId)) {
            throw new IllegalStateException("Chosen card was not exiled this way");
        }
        gameData.interaction.clearAwaitingInput();

        ExiledCardEntry entry = chosenId == null ? null : gameData.findExiledCard(chosenId);
        if (entry == null) {
            // Nothing to copy: drain through the shared queue epilogue so the parked stack entry
            // that began this choice is resumed.
            exileFreeCastQueueSupport.castNextFromQueue(gameData, interaction.playerId());
            return;
        }

        List<UUID> copyIds = new ArrayList<>();
        for (int i = 0; i < interaction.copies(); i++) {
            Card copy = copySupport.createCopyCard(entry.card());
            exileService.exileCard(gameData, interaction.playerId(), copy);
            copyIds.add(copy.getId());
        }

        log.info("Game {} - copying {} {} times for a free cast",
                gameData.id, entry.card().getName(), interaction.copies());
        exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, interaction.playerId(), copyIds);
    }
}
