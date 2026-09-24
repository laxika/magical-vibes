package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a chosen-creature-type library look through the shared top-card selection flow. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final LookAtTopCardsEffectHandler lookAtTopCardsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffect chosenTypeEffect =
                (LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffect) effect;

        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId());
            return;
        }

        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;
        gameData.rerunCurrentEffectAfterInteraction = false;

        CardAnyOfPredicate matchingCard = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(chosenSubtype),
                new CardKeywordPredicate(Keyword.CHANGELING)));
        LookAtTopCardsEffect lookEffect = new LookAtTopCardsEffect(
                new Fixed(chosenTypeEffect.count()),
                new Fixed(Integer.MAX_VALUE),
                matchingCard, LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.HAND, true);
        lookAtTopCardsEffectHandler.resolve(gameData, entry, lookEffect);
    }
}
