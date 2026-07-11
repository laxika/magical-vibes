package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.MultipleCardsChosenRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Answers "exile any number of cards named X" choices: the AI always exiles all of them.
 */
@Slf4j
class MultiZoneExileChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.MultiZoneExileChoice> {

    @Override
    public Class<PendingInteraction.MultiZoneExileChoice> handledType() {
        return PendingInteraction.MultiZoneExileChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MultiZoneExileChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = new ArrayList<>(interaction.validCardIds());
        log.info("AI: Exiling {} cards named \"{}\" in game {}", chosen.size(), interaction.cardName(), ctx.gameId());
        ctx.gameActions().handleMultipleCardsChosen(ctx.selfConnection(), new MultipleCardsChosenRequest(chosen));
    }
}
