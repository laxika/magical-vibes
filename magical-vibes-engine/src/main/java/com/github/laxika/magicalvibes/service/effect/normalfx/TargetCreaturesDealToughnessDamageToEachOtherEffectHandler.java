package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreaturesDealToughnessDamageToEachOtherEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreaturesDealToughnessDamageToEachOtherEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreaturesDealToughnessDamageToEachOtherEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreaturesDealToughnessDamageToEachOtherEffect) effect;
        List<UUID> firstGroup = entry.targetsForGroup(e.firstTargetGroup());
        List<UUID> secondGroup = entry.targetsForGroup(e.secondTargetGroup());
        if (firstGroup.isEmpty() || secondGroup.isEmpty()) {
            return;
        }

        fightSupport.dealToughnessDamageToEachOther(gameData, entry,
                gameQueryService.findPermanentById(gameData, firstGroup.getFirst()),
                gameQueryService.findPermanentById(gameData, secondGroup.getFirst()));
    }
}
