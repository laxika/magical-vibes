package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekChosenLandOrNonlandEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeekChosenLandOrNonlandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final SeekLibraryEffectHandler seekLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekChosenLandOrNonlandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekChosenLandOrNonlandEffect seekChosen = (SeekChosenLandOrNonlandEffect) effect;
        if (gameData.chosenSpellLandOrNonland == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellLandOrNonlandChoice(gameData, entry.getControllerId(), true);
            return;
        }

        boolean chooseLand = gameData.chosenSpellLandOrNonland;
        gameData.chosenSpellLandOrNonland = null;
        gameData.rerunCurrentEffectAfterInteraction = false;

        seekLibraryEffectHandler.resolve(gameData, entry, new SeekLibraryEffect(
                seekChosen.count(),
                chooseLand
                        ? new CardTypePredicate(CardType.LAND)
                        : new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                LibrarySearchDestination.HAND));
    }
}
