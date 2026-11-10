package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Caged Sun's and Gauntlet of Power's "add an additional one mana of that color" from the stack entry of the
 * triggered mana ability (CR 605.4a): when the tapped land produced the source's chosen color (or, for a tap that
 * reported none, its basic land types make it add that color), the ability's controller, or for "its controller adds"
 * the land's controller, adds one more of it.
 */
@Component
@RequiredArgsConstructor
public class AddExtraManaOfChosenColorOnLandTapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddExtraManaOfChosenColorOnLandTapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = TappedLandManaSupport.sourcePermanent(gameData, gameQueryService, entry);
        CardColor chosenColor = source == null ? null : source.getChosenColor();
        Permanent tappedLand = TappedLandManaSupport.tappedLand(gameData, gameQueryService, entry);
        if (chosenColor == null || tappedLand == null) {
            return;
        }
        ManaColor chosenManaColor = ManaColor.valueOf(chosenColor.name());
        if (!TappedLandManaSupport.producedManaTypes(entry, tappedLand).contains(chosenManaColor)) {
            return;
        }
        var mana = (AddExtraManaOfChosenColorOnLandTapEffect) effect;
        TappedLandManaSupport.addAdditionalMana(gameData, gameLogService, entry, mana.toTappedLandController()
                ? TappedLandManaSupport.tappedPermanentController(gameData, gameQueryService, entry)
                : entry.getControllerId(), chosenManaColor, 1);
    }
}
