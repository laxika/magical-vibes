package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Answers Bruna, Light of Alabaster's "attach any number of Auras" choice. The AI only takes Aura
 * cards out of its own graveyard and hand — free permanents it isn't otherwise using. Auras already
 * on the battlefield are left where they are: moving one is as likely to strip the AI's own board or
 * import an opponent's Pacifism as it is to gain anything.
 */
@Slf4j
class AttachAurasChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.AttachAurasChoice> {

    @Override
    public Class<PendingInteraction.AttachAurasChoice> handledType() {
        return PendingInteraction.AttachAurasChoice.class;
    }

    @Override
    public void answer(PendingInteraction.AttachAurasChoice interaction, AiInteractionContext ctx)
            throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        GameData gameData = ctx.gameData();
        UUID aiPlayerId = ctx.aiPlayerId();
        List<UUID> ownCardIds = new ArrayList<>();
        gameData.playerGraveyards.getOrDefault(aiPlayerId, List.of())
                .forEach(card -> ownCardIds.add(card.getId()));
        gameData.playerHands.getOrDefault(aiPlayerId, List.of())
                .forEach(card -> ownCardIds.add(card.getId()));

        List<UUID> chosen = interaction.validCardIds().stream().filter(ownCardIds::contains).toList();

        log.info("AI: Attaching {} Aura(s) to {} in game {}", chosen.size(), interaction.sourceName(),
                ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }
}
