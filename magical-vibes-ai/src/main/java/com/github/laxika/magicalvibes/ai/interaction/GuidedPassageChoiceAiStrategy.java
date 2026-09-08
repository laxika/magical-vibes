package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/** Answers Guided Passage by taking the first available card for each required category. */
@Slf4j
class GuidedPassageChoiceAiStrategy
        implements AiInteractionStrategy<PendingInteraction.GuidedPassageChoice> {

    @Override
    public Class<PendingInteraction.GuidedPassageChoice> handledType() {
        return PendingInteraction.GuidedPassageChoice.class;
    }

    @Override
    public void answer(PendingInteraction.GuidedPassageChoice interaction,
            AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> chosen = new ArrayList<>();
        chooseFirstMatching(interaction.pool(), chosen, CardType.CREATURE);
        chooseFirstMatching(interaction.pool(), chosen, CardType.LAND);
        interaction.pool().stream()
                .filter(card -> !card.hasType(CardType.CREATURE) && !card.hasType(CardType.LAND))
                .map(Card::getId)
                .filter(cardId -> !chosen.contains(cardId))
                .findFirst()
                .ifPresent(chosen::add);

        log.info("AI: Choosing {} Guided Passage cards in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(List.copyOf(chosen)));
    }

    private static void chooseFirstMatching(List<Card> pool, List<UUID> chosen, CardType type) {
        pool.stream()
                .filter(card -> card.hasType(type))
                .map(Card::getId)
                .filter(cardId -> !chosen.contains(cardId))
                .findFirst()
                .ifPresent(chosen::add);
    }
}
