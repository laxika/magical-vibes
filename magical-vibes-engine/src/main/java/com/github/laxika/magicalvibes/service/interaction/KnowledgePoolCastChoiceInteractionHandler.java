package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the Knowledge Pool cast choice: the caster of the exiled spell may cast another
 * nonland card from the pool without paying its mana cost, or decline with an empty
 * selection. Card views are re-derived from the pool at prompt time; the pool permanent is
 * found via the queued {@link PendingKnowledgePoolCast}. The answer (including the actual
 * cast) is applied by {@link ExileSupport#handleKnowledgePoolCastChoice}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgePoolCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.KnowledgePoolCastChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final ExileSupport exileSupport;

    @Override
    public Class<PendingInteraction.KnowledgePoolCastChoice> handledType() {
        return PendingInteraction.KnowledgePoolCastChoice.class;
    }

    @Override
    public AwaitingInput legacyInputType() {
        return AwaitingInput.KNOWLEDGE_POOL_CAST_CHOICE;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.KnowledgePoolCastChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.KnowledgePoolCastChoice interaction, UUID recipientId) {
        List<CardView> cardViews = new ArrayList<>();
        PendingKnowledgePoolCast pendingCast = gameData.peekPendingInteraction(PendingKnowledgePoolCast.class);
        UUID kpPermanentId = pendingCast != null ? pendingCast.sourcePermanentId() : null;
        if (kpPermanentId != null) {
            List<Card> pool = gameData.getCardsExiledByPermanent(kpPermanentId);
            for (Card card : pool) {
                if (interaction.validCardIds().contains(card.getId())) {
                    cardViews.add(cardViewFactory.create(card));
                }
            }
        }

        sessionManager.sendToPlayer(recipientId,
                new ChooseMultipleCardsMessage(new ArrayList<>(interaction.validCardIds()), cardViews,
                        interaction.maxCount(),
                        "Knowledge Pool — you may cast a nonland card without paying its mana cost."));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose a card from Knowledge Pool", gameData.id, playerName);
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.KnowledgePoolCastChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        exileSupport.handleKnowledgePoolCastChoice(gameData, player, cardIds);
    }
}
