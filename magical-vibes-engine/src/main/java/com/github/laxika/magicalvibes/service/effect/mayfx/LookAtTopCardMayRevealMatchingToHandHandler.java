package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardMayRevealMatchingToHandEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * May-ability follow-ups for {@link LookAtTopCardMayRevealMatchingToHandEffect}: matching card → may reveal to hand,
 * recorded as the resolution's {@link StepKind#REVEAL} step before the effects that follow "if you do" run; a
 * declined card may be put into the graveyard, is put on the library bottom, may be put on the library bottom, or
 * stays on top, as the effect declares. Revealing doesn't move the card (CR 701.20b): putting it into the hand does.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealMatchingToHandEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealMatchingToHandEffect)
                .map(e -> (LookAtTopCardMayRevealMatchingToHandEffect) e)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (effect.stage() == Stage.MAY_HAND) {
            if (accepted && deck != null && !deck.isEmpty()) {
                Card topCard = deck.removeFirst();
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " reveals ", topCard, " and puts it into their hand."));
                gameData.addCardToHand(controllerId, topCard);
                if (gameData.pendingEffectResolutionEntry != null) {
                    gameData.pendingEffectResolutionEntry.recordStep(StepKind.REVEAL, StepOutcome.ofObjects(
                            List.of(topCard.getId()), List.of(controllerId),
                            List.of(new MovedCard(topCard.getId(), topCard.getId(), Zone.HAND, controllerId))));
                    if (!effect.effectsIfCardPutIntoHand().isEmpty()) {
                        gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                                gameData.pendingEffectResolutionIndex,
                                effect.effectsIfCardPutIntoHand());
                    }
                }
                log.info("Game {} - {} reveals {} to hand", gameData.id, player.getUsername(), topCard.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            if (deck != null && !deck.isEmpty()) {
                declineHand(gameData, player, ability, effect, deck);
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (effect.stage() == Stage.MAY_GRAVEYARD) {
            if (accepted && deck != null && !deck.isEmpty()) {
                Card topCard = deck.removeFirst();
                graveyardService.addCardToGraveyard(gameData, controllerId, topCard, Zone.LIBRARY);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " puts ")
                        .card(topCard)
                        .text(" into their graveyard.")
                        .build());
                log.info("Game {} - {} puts {} into graveyard", gameData.id, player.getUsername(), topCard.getName());
            } else {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " leaves the card on top of their library."));
                log.info("Game {} - {} leaves top card on library", gameData.id, player.getUsername());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (effect.stage() == Stage.MAY_BOTTOM) {
            if (accepted && deck != null && !deck.isEmpty()) {
                deck.addLast(deck.removeFirst());
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " puts the card on the bottom of their library."));
            } else {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " leaves the card on top of their library."));
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    /** The matching card stays out of the hand: where the effect sends it, or the may that offers that. */
    private void declineHand(GameData gameData, Player player, PendingMayAbility ability,
                             LookAtTopCardMayRevealMatchingToHandEffect effect, List<Card> deck) {
        UUID controllerId = player.getId();
        OtherwiseDestination otherwise = effect.otherwiseDestination();
        switch (otherwise) {
            case BOTTOM -> {
                Card bottomCard = deck.removeFirst();
                deck.addLast(bottomCard);
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " puts the card on the bottom of their library."));
                log.info("Game {} - {} declines hand; puts {} on the bottom of their library",
                        gameData.id, player.getUsername(), bottomCard.getName());
            }
            case GRAVEYARD -> {
                Card topCard = deck.getFirst();
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        ability.sourceCard(),
                        controllerId,
                        List.of(effect.withStage(Stage.MAY_GRAVEYARD)),
                        ability.sourceCard().getName() + " — Put " + topCard.getName()
                                + " into your graveyard?"
                ));
                log.info("Game {} - {} declines hand; may put {} to graveyard",
                        gameData.id, player.getUsername(), topCard.getName());
            }
            case MAY_BOTTOM -> gameData.pendingMayAbilities.addFirst(LookAtTopCardMayRevealMatchingToHandEffectHandler
                    .mayBottom(ability.sourceCard(), controllerId, effect, deck.getFirst()));
            case TOP -> {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " leaves the card on top of their library."));
                log.info("Game {} - {} declines hand; card stays on top", gameData.id, player.getUsername());
            }
        }
    }
}
