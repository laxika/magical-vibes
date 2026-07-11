package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImprovisationCapstoneCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ImprovisationCapstoneCastChoice> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final ImprovisationCapstoneCastSupport improvisationCapstoneCastSupport;

    @Override
    public Class<PendingInteraction.ImprovisationCapstoneCastChoice> handledType() {
        return PendingInteraction.ImprovisationCapstoneCastChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.ImprovisationCapstoneCastChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.ImprovisationCapstoneCastChoice interaction,
                       UUID recipientId) {
        List<CardView> cardViews = new ArrayList<>();
        for (UUID cardId : interaction.validCardIds()) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry != null) {
                cardViews.add(cardViewFactory.create(entry.card()));
            }
        }

        sessionManager.sendToPlayer(recipientId,
                new ChooseMultipleCardsMessage(new ArrayList<>(interaction.validCardIds()), cardViews,
                        interaction.maxCount(),
                        "You may cast any number of spells from among the exiled cards without paying their mana costs."));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose spells for Improvisation Capstone", gameData.id, playerName);
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ImprovisationCapstoneCastChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        improvisationCapstoneCastSupport.castChosenSpellsWithoutPaying(gameData, player, cardIds);
    }
}
