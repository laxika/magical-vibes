package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutLandFromHandThenOpponentsDrawEffect;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerMayPutLandFromHandThenOpponentsDrawEffectHandler implements NormalEffectHandlerBean {

    private final EachPlayerMayPutLandFromHandThenOpponentsDrawSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayPutLandFromHandThenOpponentsDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        support.beginNextChoice(gameData, apnapOrder(gameData), new LinkedHashMap<>(),
                entry.getControllerId(), entry.getCard().getName());
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayerIds.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayerIds;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(activeIndex, orderedPlayerIds.size()));
        rotated.addAll(orderedPlayerIds.subList(0, activeIndex));
        return rotated;
    }
}
