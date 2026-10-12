package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
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

    /**
     * Whether the land already carries {@code subtype} from its own printed, granted or transient types.
     * Types supplied by other continuous effects (e.g. Prismatic Omen) are deliberately ignored, so the
     * grant is still recorded on the land and survives those effects leaving the battlefield.
     */
    private static boolean hasOwnBasicLandType(Permanent permanent, CardSubtype subtype) {
        return permanent.getCard().getSubtypes().contains(subtype)
                || permanent.getGrantedSubtypes().contains(subtype)
                || permanent.getTransientSubtypes().contains(subtype);
    }

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
            for (CardSubtype subtype : BASIC_LAND_TYPES) {
                if (!hasOwnBasicLandType(permanent, subtype)) {
                    GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(
                            permanent, subtype, EffectDuration.UNTIL_END_OF_TURN, false);
                }
            }
        }
    }
}
