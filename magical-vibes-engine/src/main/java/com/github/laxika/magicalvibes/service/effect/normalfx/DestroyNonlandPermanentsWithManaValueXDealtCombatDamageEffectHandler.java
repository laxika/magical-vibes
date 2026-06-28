package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = entry.getCard().getName();

                Set<UUID> damagedPlayerIds = gameData.combatDamageToPlayersThisTurn
                        .getOrDefault(entry.getSourcePermanentId(), Set.of());
                if (damagedPlayerIds.isEmpty()) {
                    String logEntry = cardName + " resolves but " + cardName + " dealt no combat damage to any player this turn.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} resolves but dealt no combat damage to any player this turn", gameData.id, cardName);
                    return;
                }

                destructionSupport.destroyNonlandPermanentsByManaValue(gameData, entry.getXValue(), cardName, damagedPlayerIds);
    }
}
