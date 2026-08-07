package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantCastSpellTypesThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerCantCastSpellTypesThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerCantCastSpellTypesThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        if (playerId == null) return;

        Set<CardType> restrictedTypes = ((TargetPlayerCantCastSpellTypesThisTurnEffect) effect).restrictedTypes();
        gameData.playersCantCastSpellTypesThisTurn.merge(playerId, EnumSet.copyOf(restrictedTypes), (existing, added) -> {
            Set<CardType> merged = EnumSet.copyOf(existing);
            merged.addAll(added);
            return merged;
        });

        String typeText = restrictedTypes.stream()
                .map(type -> type.name().toLowerCase())
                .collect(Collectors.joining(" or "));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(playerId) + " can't cast " + typeText + " spells this turn."));
    }
}
