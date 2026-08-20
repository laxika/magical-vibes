package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.springframework.stereotype.Component;

/**
 * The six hand-card choice handlers share one answer base while preserving each kind's decline
 * policy.
 * Prompt projection is centralized from the pending record. Answers all arrive as
 * {@link InteractionAnswer.CardIndexChosen} and delegate to the per-kind answer methods on
 * {@link CardChoiceHandlerService} / {@link AbilityActivationService}.
 */
public final class HandCardChoiceInteractionHandlers {

    private HandCardChoiceInteractionHandlers() {
    }

    private abstract static class Base<T extends PendingInteraction & PendingInteraction.HandChoice>
            implements InteractionHandler<T> {

        @Override
        public Class<? extends InteractionAnswer> answerType() {
            return InteractionAnswer.CardIndexChosen.class;
        }

        static int cardIndex(InteractionAnswer answer) {
            return ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        }
    }

    /** CARD_CHOICE — put a card from hand onto the battlefield (declinable). */
    @Component
    public static class HandCardChoiceInteractionHandler extends Base<PendingInteraction.HandCardChoice> {

        private final CardChoiceHandlerService cardChoiceHandlerService;

        public HandCardChoiceInteractionHandler(CardChoiceHandlerService cardChoiceHandlerService) {
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

        public TargetedHandCardChoiceInteractionHandler(
                CardChoiceHandlerService cardChoiceHandlerService) {
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

        public DiscardChoiceInteractionHandler(CardChoiceHandlerService cardChoiceHandlerService) {
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

        public ExileFromHandChoiceInteractionHandler(
                CardChoiceHandlerService cardChoiceHandlerService) {
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

        public ImprintFromHandChoiceInteractionHandler(
                CardChoiceHandlerService cardChoiceHandlerService) {
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

    /** EXILE_FROM_HAND_WITH_REFINE_COUNTERS_CHOICE — exile the selected card with refine counters. */
    @Component
    public static class ExileFromHandWithRefineCountersChoiceInteractionHandler
            extends Base<PendingInteraction.ExileFromHandWithRefineCountersChoice> {

        private final CardChoiceHandlerService cardChoiceHandlerService;

        public ExileFromHandWithRefineCountersChoiceInteractionHandler(
                CardChoiceHandlerService cardChoiceHandlerService) {
            this.cardChoiceHandlerService = cardChoiceHandlerService;
        }

        @Override
        public Class<PendingInteraction.ExileFromHandWithRefineCountersChoice> handledType() {
            return PendingInteraction.ExileFromHandWithRefineCountersChoice.class;
        }

        @Override
        public void handleAnswer(GameData gameData, Player player,
                                 PendingInteraction.ExileFromHandWithRefineCountersChoice interaction,
                                 InteractionAnswer answer) {
            cardChoiceHandlerService.handleExileFromHandWithRefineCountersChosen(
                    gameData, player, cardIndex(answer));
        }
    }

    /**
     * ACTIVATED_ABILITY_DISCARD_COST_CHOICE — discard a card as an activation cost. Matching
     * the originating begin site, no "Awaiting …" log line is emitted on prompt.
     */
    @Component
    public static class DiscardCostChoiceInteractionHandler extends Base<PendingInteraction.DiscardCostChoice> {

        private final AbilityActivationService abilityActivationService;

        public DiscardCostChoiceInteractionHandler(
                AbilityActivationService abilityActivationService) {
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
