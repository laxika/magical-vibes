package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellTypesNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpponentsCantCastSpellTypesNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantCastSpellTypesNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Set<CardType> restrictedTypes = ((OpponentsCantCastSpellTypesNextTurnEffect) effect).restrictedTypes();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            gameData.playersCantCastSpellTypesNextTurn.merge(playerId, EnumSet.copyOf(restrictedTypes),
                    (existing, added) -> {
                        Set<CardType> merged = EnumSet.copyOf(existing);
                        merged.addAll(added);
                        return merged;
                    });
        }

        String typeText = restrictedTypes.stream()
                .map(type -> type.name().toLowerCase())
                .collect(Collectors.joining(" or "));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId)
                        + "'s opponents can't cast " + typeText + " spells during their next turns."));
    }
}
