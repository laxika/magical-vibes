package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles the Knowledge Pool cast choice: the caster of the exiled spell may cast another
 * nonland card from the pool without paying its mana cost, or decline with an empty selection.
 * The answer (including the actual cast) is applied by
 * {@link ExileSupport#handleKnowledgePoolCastChoice}.
 */
@Component
@RequiredArgsConstructor
public class KnowledgePoolCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.KnowledgePoolCastChoice> {

    private final ExileSupport exileSupport;

    @Override
    public Class<PendingInteraction.KnowledgePoolCastChoice> handledType() {
        return PendingInteraction.KnowledgePoolCastChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(
            GameData gameData,
            Player player,
            PendingInteraction.KnowledgePoolCastChoice interaction,
            InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        exileSupport.handleKnowledgePoolCastChoice(gameData, player, cardIds);
    }
}
