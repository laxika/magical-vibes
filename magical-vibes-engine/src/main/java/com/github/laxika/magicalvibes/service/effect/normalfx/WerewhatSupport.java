package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.WerewhatOnEnterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Rules support for Werewhat's dynamic back face and linked exiled card. */
@Component
public class WerewhatSupport {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final AnimationSupport animationSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public WerewhatSupport(GameQueryService gameQueryService,
                           @Lazy PermanentRemovalService permanentRemovalService,
                           ExileService exileService,
                           AnimationSupport animationSupport,
                           @Lazy InteractionHandlerRegistry interactionHandlerRegistry) {
        this.gameQueryService = gameQueryService;
        this.permanentRemovalService = permanentRemovalService;
        this.exileService = exileService;
        this.animationSupport = animationSupport;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    /** Starts the optional hand-or-graveyard choice, if a legal creature card exists. */
    public boolean beginChoice(GameData gameData, UUID controllerId, Card card, UUID targetId,
                               boolean wasCastFromHand, int etbMode, int xValue, boolean kicked,
                               List<UUID> targetIds) {
        Permanent entering = lastEnteredPermanent(gameData, controllerId);
        if (entering == null || card.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.ON_ENTER_BATTLEFIELD)
                .stream().noneMatch(WerewhatOnEnterEffect.class::isInstance)) {
            return false;
        }

        List<UUID> validCardIds = new ArrayList<>();
        gameData.playerHands.getOrDefault(controllerId, List.of()).stream()
                .filter(this::isCreatureCard)
                .map(Card::getId)
                .forEach(validCardIds::add);
        gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(this::isCreatureCard)
                .map(Card::getId)
                .forEach(validCardIds::add);
        if (validCardIds.isEmpty()) {
            return false;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.WerewhatOnEnterChoice(
                controllerId, validCardIds, entering.getId(), controllerId, card, targetId,
                wasCastFromHand, etbMode, xValue, kicked, targetIds));
        return true;
    }

    public ChoiceResult applyChoice(GameData gameData,
                                    PendingInteraction.WerewhatOnEnterChoice interaction,
                                    UUID selectedCardId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData,
                interaction.enteringPermanentId());
        if (permanent == null) {
            return new ChoiceResult(null, false);
        }

        List<Card> hand = gameData.playerHands.getOrDefault(interaction.controllerId(), List.of());
        Card selected = hand.stream().filter(card -> card.getId().equals(selectedCardId)).findFirst().orElse(null);
        boolean fromHand = selected != null;
        if (selected == null) {
            selected = gameQueryService.findCardInGraveyardById(gameData, selectedCardId);
        }
        if (selected == null || !isCreatureCard(selected)) {
            throw new IllegalStateException("Werewhat choice is no longer available");
        }

        if (fromHand) {
            hand.removeIf(card -> card.getId().equals(selectedCardId));
        } else {
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, selectedCardId);
        }
        exileService.exileCard(gameData, interaction.controllerId(), selected, permanent.getId());

        Card dynamicBackFace = selected.createRuntimeCopy();
        Set<Keyword> backKeywords = dynamicBackFace.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(dynamicBackFace.getKeywords());
        backKeywords.add(Keyword.NIGHTBOUND);
        dynamicBackFace.setKeywords(backKeywords);
        dynamicBackFace.setBackFaceCard(null);
        dynamicBackFace.freeze();

        Card dynamicFrontFace = permanent.getOriginalCard().createRuntimeCopy();
        dynamicFrontFace.setBackFaceCard(dynamicBackFace);
        permanent.exchangeCard(dynamicFrontFace);
        permanent.setWerewhatCompanionCard(selected);

        if (gameData.dayNight == DayNight.NIGHT) {
            animationSupport.transformToBackFaceForDayNight(gameData, permanent);
        }
        return new ChoiceResult(selected, fromHand);
    }

    private Permanent lastEnteredPermanent(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield == null || battlefield.isEmpty() ? null : battlefield.getLast();
    }

    private boolean isCreatureCard(Card card) {
        return card != null && card.hasType(CardType.CREATURE);
    }

    public record ChoiceResult(Card selectedCard, boolean fromHand) {
    }
}
