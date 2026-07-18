package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Answers the Improvisation Capstone cast choice: the AI casts every exiled spell it may
 * cast without paying mana costs. The engine casts the chosen spells one at a time and
 * pauses for target choices itself, so choosing all is always safe.
 */
@Slf4j
class ImprovisationCapstoneCastChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.ImprovisationCapstoneCastChoice> {

    @Override
    public Class<PendingInteraction.ImprovisationCapstoneCastChoice> handledType() {
        return PendingInteraction.ImprovisationCapstoneCastChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ImprovisationCapstoneCastChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = new ArrayList<>(interaction.validCardIds());
        if (interaction.maxCount() > 0 && chosen.size() > interaction.maxCount()) {
            chosen = chosen.subList(0, interaction.maxCount());
        }
        log.info("AI: Casting {} exiled spells for Improvisation Capstone in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(ctx.selfConnection(), new InteractionAnswer.CardsChosen(chosen));
    }
}
