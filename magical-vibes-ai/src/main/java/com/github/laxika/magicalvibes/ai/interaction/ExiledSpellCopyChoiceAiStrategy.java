package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Answers the exiled-spell copy choice (Chandra, Pyromaster's ultimate). Copying is always
 * upside, so the AI takes the first eligible card; the engine casts the copies one at a time and
 * pauses for their targets itself.
 */
@Slf4j
class ExiledSpellCopyChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ExiledSpellCopyChoice> {

    @Override
    public Class<PendingInteraction.ExiledSpellCopyChoice> handledType() {
        return PendingInteraction.ExiledSpellCopyChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ExiledSpellCopyChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }
        List<UUID> valid = interaction.validCardIds();
        if (valid.isEmpty()) {
            ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.of()));
            return;
        }
        log.info("AI: Choosing an exiled spell to copy {} times in game {}", interaction.copies(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.of(valid.getFirst())));
    }
}
