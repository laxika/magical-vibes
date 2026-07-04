package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * The six hand-card choice handlers (the legacy shared {@code InteractionContext.CardChoice}
 * family). Each kind sends the same {@code ChooseCardFromHandMessage} with the record's
 * begin-time indices and prompt; only the decline flag and the
 * per-kind "Awaiting …" log line differ, so they share a base class. Answers all arrive as
 * {@link InteractionAnswer.CardIndexChosen} and delegate to the legacy per-kind answer
 * methods on {@link CardChoiceHandlerService} / {@link AbilityActivationService}.
 */
public final class HandCardChoiceInteractionHandlers {

    private HandCardChoiceInteractionHandlers() {
    }

    @Slf4j
    private abstract static class Base<T extends PendingInteraction & PendingInteraction.HandChoice>
            implements InteractionHandler<T> {

        private final SessionManager sessionManager;
        private final boolean canDecline;
        private final String awaitingLogSuffix;

        private Base(SessionManager sessionManager, boolean canDecline,
                     String awaitingLogSuffix) {
            this.sessionManager = sessionManager;
            this.canDecline = canDecline;
            this.awaitingLogSuffix = awaitingLogSuffix;
        }

        @Override
        public Class<? extends InteractionAnswer> answerType() {
            return InteractionAnswer.CardIndexChosen.class;
        }

        @Override
        public UUID decidingPlayerId(T interaction) {
            return interaction.playerId();
        }

        @Override
        public void prompt(GameData gameData, T interaction, UUID recipientId) {
            sessionManager.sendToPlayer(recipientId, new ChooseCardFromHandMessage(
                    interaction.validIndices(), interaction.prompt(), canDecline));

            if (awaitingLogSuffix != null) {
                String playerName = gameData.playerIdToName.get(interaction.playerId());
                log.info("Game {} - Awaiting {} {}", gameData.id, playerName, awaitingLogSuffix);
            }
        }

        static int cardIndex(InteractionAnswer answer) {
            return ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        }
    }

    /** CARD_CHOICE — put a card from hand onto the battlefield (declinable). */
    @Component
    public static class HandCardChoiceInteractionHandler extends Base<PendingInteraction.HandCardChoice> {

        private final CardChoiceHandlerService cardChoiceHandlerService;

        public HandCardChoiceInteractionHandler(SessionManager sessionManager,
                                                CardChoiceHandlerService cardChoiceHandlerService) {
            super(sessionManager, true, "to choose a card from hand");
            this.cardChoiceHandlerService = cardChoiceHandlerService;
        }

        @Override
        public Class<PendingInteraction.HandCardChoice> handledType() {
            return PendingInteraction.HandCardChoice.class;
        }

        @Override
        public void handleAnswer(GameData gameData, Player player, PendingInteraction.HandCardChoice interaction,
                                 InteractionAnswer answer) {
            cardChoiceHandlerService.handleHandCardChosen(gameData, player, cardIndex(answer));
        }
    }

    /** TARGETED_CARD_CHOICE — put an Aura from hand onto the battlefield attached to a target (declinable). */
    @Component
    public static class TargetedHandCardChoiceInteractionHandler extends Base<PendingInteraction.TargetedHandCardChoice> {

        private final CardChoiceHandlerService cardChoiceHandlerService;

        public TargetedHandCardChoiceInteractionHandler(SessionManager sessionManager,
                                                        CardChoiceHandlerService cardChoiceHandlerService) {
            super(sessionManager, true, "to choose a card from hand (targeted)");
            this.cardChoiceHandlerService = cardChoiceHandlerService;
        }

        @Override
        public Class<PendingInteraction.TargetedHandCardChoice> handledType() {
            return PendingInteraction.TargetedHandCardChoice.class;
        }

        @Override
        public void handleAnswer(GameData gameData, Player player, PendingInteraction.TargetedHandCardChoice interaction,
                                 InteractionAnswer answer) {
            cardChoiceHandlerService.handleHandCardChosen(gameData, player, cardIndex(answer));
        }
    }

    /** DISCARD_CHOICE — discard a card from hand (multi-pick countdown on the record). */
    @Component
    public static class DiscardChoiceInteractionHandler extends Base<PendingInteraction.DiscardChoice> {

        private final CardChoiceHandlerService cardChoiceHandlerService;

        public DiscardChoiceInteractionHandler(SessionManager sessionManager,
                                               CardChoiceHandlerService cardChoiceHandlerService) {
            super(sessionManager, false, "to choose a card to discard");
            this.cardChoiceHandlerService = cardChoiceHandlerService;
        }

        @Override
        public Class<PendingInteraction.DiscardChoice> handledType() {
            return PendingInteraction.DiscardChoice.class;
        }

        @Override
        public void handleAnswer(GameData gameData, Player player, PendingInteraction.DiscardChoice interaction,
                                 InteractionAnswer answer) {
            cardChoiceHandlerService.handleDiscardCardChosen(gameData, player, cardIndex(answer));
        }
    }

    /** EXILE_FROM_HAND_CHOICE — exile a card from hand (multi-pick countdown on the record). */
    @Component
    public static class ExileFromHandChoiceInteractionHandler extends Base<PendingInteraction.ExileFromHandChoice> {

        private final CardChoiceHandlerService cardChoiceHandlerService;

        public ExileFromHandChoiceInteractionHandler(SessionManager sessionManager,
                                                     CardChoiceHandlerService cardChoiceHandlerService) {
            super(sessionManager, false, "to choose a card to exile from hand");
            this.cardChoiceHandlerService = cardChoiceHandlerService;
        }

        @Override
        public Class<PendingInteraction.ExileFromHandChoice> handledType() {
            return PendingInteraction.ExileFromHandChoice.class;
        }

        @Override
        public void handleAnswer(GameData gameData, Player player, PendingInteraction.ExileFromHandChoice interaction,
                                 InteractionAnswer answer) {
            cardChoiceHandlerService.handleExileFromHandChosen(gameData, player, cardIndex(answer));
        }
    }

    /** IMPRINT_FROM_HAND_CHOICE — exile a card from hand and imprint it on the source permanent. */
    @Component
    public static class ImprintFromHandChoiceInteractionHandler extends Base<PendingInteraction.ImprintFromHandChoice> {

        private final CardChoiceHandlerService cardChoiceHandlerService;

        public ImprintFromHandChoiceInteractionHandler(SessionManager sessionManager,
                                                       CardChoiceHandlerService cardChoiceHandlerService) {
            super(sessionManager, false, "to choose an artifact from hand to imprint");
            this.cardChoiceHandlerService = cardChoiceHandlerService;
        }

        @Override
        public Class<PendingInteraction.ImprintFromHandChoice> handledType() {
            return PendingInteraction.ImprintFromHandChoice.class;
        }

        @Override
        public void handleAnswer(GameData gameData, Player player, PendingInteraction.ImprintFromHandChoice interaction,
                                 InteractionAnswer answer) {
            cardChoiceHandlerService.handleImprintFromHandCardChosen(gameData, player, cardIndex(answer));
        }
    }

    /**
     * ACTIVATED_ABILITY_DISCARD_COST_CHOICE — discard a card as an activation cost. Matching
     * the legacy begin site, no "Awaiting …" log line is emitted on prompt.
     */
    @Component
    public static class DiscardCostChoiceInteractionHandler extends Base<PendingInteraction.DiscardCostChoice> {

        private final AbilityActivationService abilityActivationService;

        public DiscardCostChoiceInteractionHandler(SessionManager sessionManager,
                                                   AbilityActivationService abilityActivationService) {
            super(sessionManager, false, null);
            this.abilityActivationService = abilityActivationService;
        }

        @Override
        public Class<PendingInteraction.DiscardCostChoice> handledType() {
            return PendingInteraction.DiscardCostChoice.class;
        }

        @Override
        public void handleAnswer(GameData gameData, Player player, PendingInteraction.DiscardCostChoice interaction,
                                 InteractionAnswer answer) {
            abilityActivationService.handleActivatedAbilityDiscardCostChosen(gameData, player, cardIndex(answer));
        }
    }
}
