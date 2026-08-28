package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the mixed hand and land selection used by Choco, Seeker of Paradise. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect typedEffect =
                (LookAtTopCardsChooseOneToHandThenLandsToBattlefieldTappedEffect) effect;
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, typedEffect.count(), AmountContext.forStackEntry(entry, null)));
        if (count == 0) {
            return;
        }

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, count, true);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, topCards.stream().map(Card::getId).toList(),
                true, true, false, false, false, 0, null, 1,
                "You may put one of them into your hand.", false, 0, false,
                null, false, null, true, false));
    }
}
