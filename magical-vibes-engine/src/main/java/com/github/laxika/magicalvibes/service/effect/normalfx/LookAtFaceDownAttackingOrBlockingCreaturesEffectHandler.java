package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownAttackingOrBlockingCreaturesEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LookAtFaceDownAttackingOrBlockingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtFaceDownAttackingOrBlockingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
            for (Permanent permanent : battlefield) {
                if (permanent.isFaceDown()
                        && gameQueryService.isCreature(gameData, permanent)
                        && (permanent.isAttacking() || permanent.isBlocking())) {
                    cardRevealService.lookAtFaceDownPermanent(gameData, entry.getControllerId(), permanent);
                }
            }
        }
    }
}
