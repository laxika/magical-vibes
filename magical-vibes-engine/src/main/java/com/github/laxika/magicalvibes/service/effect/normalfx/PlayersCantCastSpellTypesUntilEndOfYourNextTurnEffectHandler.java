package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Set<CardType> restrictedTypes =
                ((PlayersCantCastSpellTypesUntilEndOfYourNextTurnEffect) effect).restrictedTypes();
        int expirationTurn = gameData.turnsTakenByPlayer.getOrDefault(controllerId, 0) + 1;
        Map<CardType, Integer> restrictions = gameData.playersCantCastSpellTypesUntilEndOfControllerNextTurn
                .computeIfAbsent(controllerId, ignored -> new java.util.concurrent.ConcurrentHashMap<>());
        restrictedTypes.forEach(type -> restrictions.merge(type, expirationTurn, Math::max));

        String typeText = restrictedTypes.stream()
                .map(type -> type.name().toLowerCase())
                .collect(Collectors.joining(" or "));
        gameLogService.append(gameData, GameLog.text(
                "Players can't cast " + typeText + " spells until the end of "
                        + gameData.playerIdToName.get(controllerId) + "'s next turn."));
    }
}
