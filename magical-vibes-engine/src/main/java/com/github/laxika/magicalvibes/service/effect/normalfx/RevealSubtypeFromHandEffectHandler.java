package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RevealSubtypeFromHandEffectHandler implements NormalEffectHandlerBean, com.github.laxika.magicalvibes.service.effect.mayfx.MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.CardRevealService cardRevealService;
    private final com.github.laxika.magicalvibes.service.input.InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealSubtypeFromHandEffect.class;
    }

    /**
     * Reveals a card of the subtype from the controller's hand, chosen when several match, and then performs the
     * effect's {@code thenEffect}; with none in hand it does nothing. Inside a "you may" it runs once the choice is
     * accepted ({@link MayEffectHandler} doesn't ask when nothing could be revealed).
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealSubtypeFromHandEffect reveal = (RevealSubtypeFromHandEffect) effect;
        List<Card> matching = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> gameQueryService.cardHasSubtype(
                        card, reveal.subtype(), gameData, entry.getControllerId()))
                .toList();
        if (matching.isEmpty()) {
            return;
        }
        if (matching.size() > 1) {
            interactionHandlerRegistry.begin(gameData,
                    new com.github.laxika.magicalvibes.model.PendingInteraction.RevealedMatchingHandCardChoice(
                            entry.getControllerId(), entry.getControllerId(), matching,
                            reveal.thenEffect(), "Choose a " + reveal.subtype().name().toLowerCase()
                            + " card to reveal from your hand.", true));
            return;
        }
        cardRevealService.revealMatchingHandCardsToAllPlayers(
                gameData, entry.getControllerId(), List.of(matching.getFirst()));
        int effectIndex = entry.getResolvingEffectIndex();
        if (effectIndex < 0) effectIndex = entry.getEffectsToResolve().indexOf(effect);
        entry.insertEffectsToResolve(effectIndex + 1, List.of(reveal.thenEffect()));
    }

    @Override
    public void handle(GameData gameData, com.github.laxika.magicalvibes.model.Player player,
                       boolean accepted, com.github.laxika.magicalvibes.model.PendingMayAbility ability) {
        RevealSubtypeFromHandEffect reveal = ability.effects().stream()
                .filter(RevealSubtypeFromHandEffect.class::isInstance)
                .map(RevealSubtypeFromHandEffect.class::cast).findFirst().orElseThrow();
        if (accepted) {
            Card card = gameData.playerHands.getOrDefault(ability.controllerId(), List.of()).stream()
                    .filter(candidate -> gameQueryService.cardHasSubtype(
                            candidate, reveal.subtype(), gameData, ability.controllerId()))
                    .findFirst().orElse(null);
            if (card != null) {
                cardRevealService.revealMatchingHandCardsToAllPlayers(
                        gameData, ability.controllerId(), List.of(card));
                if (gameData.pendingEffectResolutionEntry != null) {
                    gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                            gameData.pendingEffectResolutionIndex, List.of(reveal.thenEffect()));
                }
            }
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
