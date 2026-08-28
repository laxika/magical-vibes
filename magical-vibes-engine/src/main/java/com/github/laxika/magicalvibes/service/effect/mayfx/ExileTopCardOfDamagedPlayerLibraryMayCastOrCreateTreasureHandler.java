package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.VaanExileCastSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureHandler implements MayEffectHandlerBean {

    private final VaanExileCastSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            support.beginCast(gameData, player, ability.sourceCard(), ability.targetCardId(),
                    ability.sourcePermanentId());
        } else {
            support.createTreasure(gameData, ability.controllerId(), ability.sourceCard(),
                    ability.sourcePermanentId());
        }
    }
}
