package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardFromTargetHandForValkiEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentRevealsHandAndExilesCreatureCardEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachOpponentRevealsHandAndExilesCreatureCardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentRevealsHandAndExilesCreatureCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        List<CardEffect> choices = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .map(ChooseCreatureCardFromTargetHandForValkiEffect::new)
                .map(effectToResolve -> (CardEffect) effectToResolve)
                .toList();
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (!choices.isEmpty() && effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, choices);
        }
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard() == entry.getCard()
                        || permanent.getCard().getId().equals(entry.getCard().getId())) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }
}
