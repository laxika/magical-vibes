package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardByIdFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnFromGraveyardInsteadOfLearnEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves the shared Learn event, including Learn replacement effects in graveyards. */
@Component
@RequiredArgsConstructor
public class LearnEffectHandler implements NormalEffectHandlerBean {

    private static final CardEffect LESSON_SEARCH = new SearchOutsideGameForCardToHandEffect(
            new CardSubtypePredicate(CardSubtype.LESSON));

    private final MayEffectHandler mayEffectHandler;
    private final SearchOutsideGameForCardToHandEffectHandler searchHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LearnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LearnEffect learn = (LearnEffect) effect;
        int effectIndex = findEffectIndex(entry, effect);
        if (effectIndex < 0) {
            return;
        }

        Card replacementCard = learn.checkReplacement()
                ? findReplacementCard(gameData, entry, learn.excludedReplacementCardIds())
                : null;
        if (replacementCard != null) {
            Set<UUID> excluded = new HashSet<>(learn.excludedReplacementCardIds());
            excluded.add(replacementCard.getId());
            MayEffect replacementChoice = new MayEffect(
                    new ReturnCardByIdFromGraveyardToBattlefieldEffect(replacementCard.getId()),
                    "Return " + replacementCard.getName() + " to the battlefield instead of learning?",
                    new LearnEffect(true, Set.copyOf(excluded)));
            entry.replaceEffectToResolve(effectIndex, replacementChoice);
            mayEffectHandler.resolve(gameData, entry, replacementChoice);
            return;
        }

        if (gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).isEmpty()) {
            entry.replaceEffectToResolve(effectIndex, LESSON_SEARCH);
            searchHandler.resolve(gameData, entry, LESSON_SEARCH);
        } else {
            MayEffect discardChoice = new MayEffect(
                    new DiscardAndDrawCardEffect(),
                    "Discard a card to draw a card?",
                    LESSON_SEARCH);
            entry.replaceEffectToResolve(effectIndex, discardChoice);
            mayEffectHandler.resolve(gameData, entry, discardChoice);
        }
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        int directIndex = entry.getEffectsToResolve().indexOf(effect);
        if (directIndex >= 0) {
            return directIndex;
        }
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect parent = entry.getEffectsToResolve().get(i);
            if (parent instanceof ConditionalEffect conditional && conditional.wrapped() == effect) {
                return i;
            }
            if (parent instanceof MayEffect may
                    && (may.wrapped() == effect || may.elseEffect() == effect)) {
                return i;
            }
        }
        return -1;
    }

    private Card findReplacementCard(GameData gameData, StackEntry entry, Set<UUID> excludedCardIds) {
        return gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> !excludedCardIds.contains(card.getId()))
                .filter(card -> card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(ReturnFromGraveyardInsteadOfLearnEffect.class::isInstance))
                .findFirst()
                .orElse(null);
    }
}
