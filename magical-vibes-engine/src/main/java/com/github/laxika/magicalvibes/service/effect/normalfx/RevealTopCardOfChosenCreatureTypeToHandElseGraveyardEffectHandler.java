package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfChosenCreatureTypeToHandElseGraveyardEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a top-library reveal that first asks the controller to choose a creature type. */
@Component
@RequiredArgsConstructor
public class RevealTopCardOfChosenCreatureTypeToHandElseGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final RevealTopCardMatchingToHandElseGraveyardEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfChosenCreatureTypeToHandElseGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        CardPredicate matchingCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(chosenSubtype),
                        new CardKeywordPredicate(Keyword.CHANGELING)
                ))
        ));
        revealHandler.resolve(gameData, entry,
                new RevealTopCardMatchingToHandElseGraveyardEffect(matchingCreature));
    }
}
