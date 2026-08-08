package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapLandsThatCouldProduceSameManaAsTappedLandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Mana Web's deferred land-tap trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TapLandsThatCouldProduceSameManaAsTappedLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LandManaTypeSupport landManaTypeSupport;
    private final TapUntapSupport tapUntapSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapLandsThatCouldProduceSameManaAsTappedLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        Permanent triggeringLand = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (playerId == null || triggeringLand == null) {
            return;
        }

        Set<ManaColor> manaTypes = landManaTypeSupport.manaTypesCouldProduce(gameData, triggeringLand);
        if (manaTypes.isEmpty()) {
            return;
        }

        var battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> matchingLands = new ArrayList<>();
        for (Permanent land : new ArrayList<>(battlefield)) {
            if (!land.getCard().hasType(CardType.LAND)
                    || !manaTypesOverlap(gameData, land, manaTypes)) {
                continue;
            }
            matchingLands.add(land);
        }

        int tappedCount = 0;
        for (Permanent land : matchingLands) {
            if (tapUntapSupport.tapPermanent(gameData, land)) {
                tappedCount++;
            }
        }

        gameLogService.append(gameData,
                GameLog.cardThen(entry.getCard(), " taps " + tappedCount + " matching land(s)."));
        log.info("Game {} - {} taps {} matching lands", gameData.id, entry.getCard().getName(), tappedCount);
    }

    private boolean manaTypesOverlap(GameData gameData, Permanent land,
                                     Set<ManaColor> manaTypes) {
        for (var type : landManaTypeSupport.manaTypesCouldProduce(gameData, land)) {
            if (manaTypes.contains(type)) {
                return true;
            }
        }
        return false;
    }
}
