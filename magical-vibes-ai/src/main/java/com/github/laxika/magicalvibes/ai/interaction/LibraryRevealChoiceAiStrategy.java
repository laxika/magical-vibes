package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.MultipleCardsChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Answers library reveal choices (Lead the Stampede, Commune with Nature, Sword-Point
 * Diplomacy, …): the AI chooses all valid cards, except for punisher reveals (a life cost per
 * selection), where it denies nothing to avoid paying life. Ported verbatim from the legacy
 * {@code AiChoiceHandler} block.
 */
@Slf4j
class LibraryRevealChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.LibraryRevealChoice> {

    @Override
    public Class<PendingInteraction.LibraryRevealChoice> handledType() {
        return PendingInteraction.LibraryRevealChoice.class;
    }

    @Override
    public void answer(PendingInteraction.LibraryRevealChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen;
        if (interaction.lifeCostPerSelection() > 0) {
            // Punisher reveal (e.g. Sword-Point Diplomacy): selecting cards costs life.
            // AI denies nothing to avoid paying life.
            chosen = List.of();
            log.info("AI: Denying 0 revealed cards (punisher reveal, {} life each) in game {}",
                    interaction.lifeCostPerSelection(), ctx.gameId());
        } else {
            chosen = new ArrayList<>(interaction.validCardIds());
            log.info("AI: Choosing {} revealed cards in game {}", chosen.size(), ctx.gameId());
        }
        ctx.gameActions().handleMultipleCardsChosen(ctx.selfConnection(), new MultipleCardsChosenRequest(chosen));
    }
}
