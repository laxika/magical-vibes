package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves "add an additional {C}" from the stack entry of the tap-for-mana triggered mana ability (CR 605.4a): the
 * ability's controller adds one mana of the color, or for "its controller adds" the tapped permanent's controller.
 */
@Component
@RequiredArgsConstructor
public class AddManaWhenTappedForManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaWhenTappedForManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mana = (AddManaWhenTappedForManaEffect) effect;
        TappedLandManaSupport.addAdditionalMana(gameData, gameLogService, entry, mana.toTappedPermanentController()
                ? TappedLandManaSupport.tappedPermanentController(gameData, gameQueryService, entry)
                : entry.getControllerId(), mana.color(), 1);
    }
}
