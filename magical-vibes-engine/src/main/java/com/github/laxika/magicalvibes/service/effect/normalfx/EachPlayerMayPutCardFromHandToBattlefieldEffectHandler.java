package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerMayPutCardFromHandToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final EachPlayerMayPutCardFromHandToBattlefieldSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayPutCardFromHandToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayPutCardFromHandToBattlefieldEffect typedEffect =
                (EachPlayerMayPutCardFromHandToBattlefieldEffect) effect;
        List<UUID> players = apnapOrder(gameData);
        if (typedEffect.opponentsOnly()) {
            players.removeIf(playerId -> playerId.equals(entry.getControllerId()));
        }
        support.beginNextChoice(gameData, players, List.of(), typedEffect, entry.getCard().getName());
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
