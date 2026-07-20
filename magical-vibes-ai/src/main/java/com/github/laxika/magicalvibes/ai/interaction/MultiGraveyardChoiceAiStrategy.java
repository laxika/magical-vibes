package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Answers multi-graveyard card selections: the AI takes the first cards up to the maximum. When the
 * choice must come from a single graveyard ("... from a single graveyard", Scarab Feast) the AI
 * confines its picks to one graveyard — the one holding the most selectable cards.
 */
@Slf4j
class MultiGraveyardChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.MultiGraveyardChoice> {

    @Override
    public Class<PendingInteraction.MultiGraveyardChoice> handledType() {
        return PendingInteraction.MultiGraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MultiGraveyardChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> validIds = interaction.validCardIds();
        if (validIds.isEmpty()) {
            return;
        }

        if (ctx.gameData().graveyardTargetOperation.singleGraveyard) {
            validIds = confineToSingleGraveyard(validIds, ctx);
        }

        List<UUID> chosen = validIds.stream().limit(interaction.maxCount()).toList();

        log.info("AI: Choosing {} graveyard cards in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }

    /** Keep only the cards belonging to whichever graveyard holds the most selectable cards. */
    private List<UUID> confineToSingleGraveyard(List<UUID> validIds, AiInteractionContext ctx) {
        Map<UUID, List<UUID>> byOwner = new LinkedHashMap<>();
        for (UUID cardId : validIds) {
            UUID owner = ctx.gameQueryService().findGraveyardOwnerById(ctx.gameData(), cardId);
            byOwner.computeIfAbsent(owner, k -> new java.util.ArrayList<>()).add(cardId);
        }
        return byOwner.values().stream()
                .max(java.util.Comparator.comparingInt(List::size))
                .orElse(validIds);
    }
}
