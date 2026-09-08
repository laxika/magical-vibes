package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastRippleCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RippleFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one optional same-name free cast from Ripple. */
@Component
@RequiredArgsConstructor
public class MayCastRippleCardWithoutPayingHandler implements MayEffectHandlerBean {

    private final RippleFreeCastSupport rippleFreeCastSupport;
    private final MayCastHandlerService mayCastHandlerService;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastRippleCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        PendingInteraction.RippleFreeCastGroup group =
                gameData.pollPendingInteraction(PendingInteraction.RippleFreeCastGroup.class);
        if (group == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card card = ability.sourceCard();
        List<Card> heldCards = new ArrayList<>(group.heldCards());
        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to cast ", card, "."));
            if (rippleFreeCastSupport.hasPendingOffers(gameData)) {
                gameData.queueInteraction(new PendingInteraction.RippleFreeCastGroup(
                        group.ownerId(), group.casterId(), group.cardName(), heldCards));
            } else {
                rippleFreeCastSupport.beginBottomReorder(gameData, group.ownerId(), heldCards);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        rippleFreeCastSupport.clearPendingOffers(gameData);
        heldCards.removeIf(heldCard -> heldCard.getId().equals(card.getId()));
        boolean hasMoreSameName = heldCards.stream()
                .anyMatch(heldCard -> group.cardName().equals(heldCard.getName()));
        if (hasMoreSameName) {
            rippleFreeCastSupport.offerOrBottom(
                    gameData, group.ownerId(), group.casterId(), group.cardName(), heldCards);
        }

        List<Card> cardsToBottom = hasMoreSameName ? null : heldCards;
        mayCastHandlerService.castRevealedCardWithoutPaying(
                gameData, player, card, group.ownerId(), cardsToBottom, true);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
