package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OwnLandsGainAllBasicLandTypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OwnLandsGainAllBasicLandTypesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private static final List<CardSubtype> BASIC_LAND_TYPES = List.of(
            CardSubtype.PLAINS,
            CardSubtype.ISLAND,
            CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN,
            CardSubtype.FOREST
    );

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OwnLandsGainAllBasicLandTypesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isLand(gameData, permanent)) {
                continue;
            }
            // Every type, also one the land has now: an older effect that set its land types (Blood Moon on a Taiga)
            // is overridden by this later grant in timestamp order (CR 613.7), and a type it gets from another effect
            // stays granted when that effect ends.
            for (CardSubtype subtype : BASIC_LAND_TYPES) {
                GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(gameData, permanent, subtype,
                        Duration.UNTIL_END_OF_TURN, false, entry.getCard().getName(),
                        entry.getSourcePermanentId(), entry.getControllerId());
            }
        }
    }
}
