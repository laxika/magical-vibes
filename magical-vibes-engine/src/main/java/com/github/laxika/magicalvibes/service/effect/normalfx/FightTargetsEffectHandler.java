package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FightTargetsEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FightTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FightTargetsEffect) effect;

        List<UUID> firstGroup = entry.targetsForGroup(e.firstTargetGroup());
        List<UUID> secondGroup = entry.targetsForGroup(e.secondTargetGroup());
        if (firstGroup.isEmpty() || secondGroup.isEmpty()) {
            return; // Optional target not chosen ("up to one") — no fight happens
        }

        fightSupport.fight(gameData, entry,
                gameQueryService.findPermanentById(gameData, firstGroup.getFirst()),
                gameQueryService.findPermanentById(gameData, secondGroup.getFirst()));
    }
}
