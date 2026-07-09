package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AshlingThePilgrimEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Ashling the Pilgrim's {@code {1}{R}} activated ability: put a +1/+1 counter on Ashling,
 * then, if this is the third time the ability has resolved this turn, remove all +1/+1 counters and
 * have Ashling deal that much damage to each creature and each player.
 *
 * <p>Resolutions are counted per source permanent in {@link GameData#permanentAbilityResolutionsThisTurn}
 * (reset each turn), so the bonus fires on the exact third resolution and not on any later one.
 */
@Component
@RequiredArgsConstructor
public class AshlingThePilgrimEffectHandler implements NormalEffectHandlerBean {

    private static final int EXPLOSION_RESOLUTION = 3;

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final DamageSupport damageSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AshlingThePilgrimEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, self, CounterType.PLUS_ONE_PLUS_ONE, 1);

        int resolutions = gameData.permanentAbilityResolutionsThisTurn.merge(self.getId(), 1, Integer::sum);
        if (resolutions != EXPLOSION_RESOLUTION) {
            return;
        }

        int removed = self.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        self.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        gameBroadcastService.logAndBroadcast(gameData,
                self.getCard().getName() + " removes all +1/+1 counters and deals " + removed
                        + " damage to each creature and each player.");

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        damageSupport.damageAllCreaturesOnBattlefield(gameData, entry, removed,
                p -> gameQueryService.isCreature(gameData, p));
        for (UUID playerId : gameData.orderedPlayerIds) {
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, removed);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
