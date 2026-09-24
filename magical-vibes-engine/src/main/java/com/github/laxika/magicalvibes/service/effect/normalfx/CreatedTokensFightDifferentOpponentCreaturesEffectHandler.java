package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreatedTokensFightDifferentOpponentCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the one-to-one fights in Ezuri's Predation after its tokens enter. */
@Component
@RequiredArgsConstructor
public class CreatedTokensFightDifferentOpponentCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreatedTokensFightDifferentOpponentCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> opponentCreatures = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (entry.getControllerId().equals(playerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    opponentCreatures.add(permanent);
                }
            }
        }

        int fightCount = Math.min(entry.getCreatedPermanentIds().size(), opponentCreatures.size());
        for (int i = 0; i < fightCount; i++) {
            Permanent token = gameQueryService.findPermanentById(
                    gameData, entry.getCreatedPermanentIds().get(i));
            fightSupport.fight(gameData, entry, token, opponentCreatures.get(i));
        }
    }
}
