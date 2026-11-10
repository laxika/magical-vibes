package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddRestrictedManaWhenLandTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Snowfall's "its controller may add an additional {U} … {U}{U} instead … spend this mana only …" from the
 * stack entry of the triggered mana ability (CR 605.4a): the land's controller adds the restricted mana, the
 * snow amount when the land, as it is or as it last existed, is snow.
 */
@Component
@RequiredArgsConstructor
public class AddRestrictedManaWhenLandTappedForManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddRestrictedManaWhenLandTappedForManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mana = (AddRestrictedManaWhenLandTappedForManaEffect) effect;
        Permanent tappedLand = TappedLandManaSupport.tappedLand(gameData, gameQueryService, entry);
        UUID recipientId = TappedLandManaSupport.tappedPermanentController(gameData, gameQueryService, entry);
        ManaPool pool = recipientId == null ? null : gameData.playerManaPools.get(recipientId);
        if (tappedLand == null || pool == null) {
            return;
        }
        boolean snow = gameQueryService.hasEffectiveSupertype(gameData, tappedLand, CardSupertype.SNOW);
        int amount = snow ? mana.snowAmount() : mana.amount();
        mana.restriction().applyTo(pool, mana.color(), amount);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " triggers — " + gameData.playerIdToName.get(recipientId)
                        + " adds " + amount + " additional " + mana.color().name().toLowerCase()
                        + " mana (" + mana.restriction().description() + ")."));
    }
}
