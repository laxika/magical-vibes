package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilChosenLandOrNonlandToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealUntilChosenLandOrNonlandToHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilChosenLandOrNonlandToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellLandOrNonland == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellLandOrNonlandChoice(gameData, entry.getControllerId());
            return;
        }

        boolean chooseLand = gameData.chosenSpellLandOrNonland;
        gameData.chosenSpellLandOrNonland = null;
        gameData.rerunCurrentEffectAfterInteraction = false;

        revealHandler.resolve(gameData, entry, new RevealUntilCardPredicateRestOnBottomRandomEffect(
                chooseLand
                        ? new CardTypePredicate(CardType.LAND)
                        : new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                LibrarySearchDestination.HAND));
    }
}
