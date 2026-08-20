package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopCardFromLibraryForManaEffect;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CastTopCardFromLibraryForManaEffectHandler implements NormalEffectHandlerBean {

    private final LibraryChoiceHandlerService libraryChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTopCardFromLibraryForManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CastTopCardFromLibraryForManaEffect) effect;
        libraryChoiceHandlerService.castTopCardFromLibraryForMana(
                gameData, entry.getControllerId(), e.cardToCast(), e.manaCost());
    }
}
