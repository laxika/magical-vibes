package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MayRevealSubtypeFromHandEffectHandler implements NormalEffectHandlerBean, com.github.laxika.magicalvibes.service.effect.mayfx.MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final MayEffectHandler mayEffectHandler;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.CardRevealService cardRevealService;
    private final com.github.laxika.magicalvibes.service.input.InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayRevealSubtypeFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayRevealSubtypeFromHandEffect reveal = (MayRevealSubtypeFromHandEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.stream().noneMatch(card -> gameQueryService.cardHasSubtype(
                card, reveal.subtype(), gameData, entry.getControllerId()))) {
            return;
        }
        int effectIndex = entry.getResolvingEffectIndex();
        if (effectIndex >= 0 && entry.getEffectsToResolve().get(effectIndex) instanceof MayEffect) {
            List<Card> matching = hand.stream().filter(card -> gameQueryService.cardHasSubtype(
                    card, reveal.subtype(), gameData, entry.getControllerId())).toList();
            if (matching.size() > 1) {
                interactionHandlerRegistry.begin(gameData,
                        new com.github.laxika.magicalvibes.model.PendingInteraction.RevealedMatchingHandCardChoice(
                                entry.getControllerId(), entry.getControllerId(), matching,
                                reveal.thenEffect(), "Choose a " + reveal.subtype().name().toLowerCase()
                                + " card to reveal from your hand.", true));
                return;
            }
            Card chosen = matching.getFirst();
            cardRevealService.revealMatchingHandCardsToAllPlayers(
                    gameData, entry.getControllerId(), List.of(chosen));
            entry.insertEffectsToResolve(effectIndex + 1, List.of(reveal.thenEffect()));
            return;
        }
        MayEffect may = new MayEffect(reveal, reveal.prompt());
        if (effectIndex < 0) effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) throw new IllegalStateException("Reveal effect is not on its stack entry");
        entry.replaceEffectToResolve(effectIndex, may);
        mayEffectHandler.resolve(gameData, entry, may);
    }

    @Override
    public void handle(GameData gameData, com.github.laxika.magicalvibes.model.Player player,
                       boolean accepted, com.github.laxika.magicalvibes.model.PendingMayAbility ability) {
        MayRevealSubtypeFromHandEffect reveal = ability.effects().stream()
                .filter(MayRevealSubtypeFromHandEffect.class::isInstance)
                .map(MayRevealSubtypeFromHandEffect.class::cast).findFirst().orElseThrow();
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
