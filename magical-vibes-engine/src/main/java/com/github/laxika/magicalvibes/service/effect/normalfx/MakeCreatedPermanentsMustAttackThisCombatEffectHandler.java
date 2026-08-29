package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MakeCreatedPermanentsMustAttackThisCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatedPermanentsMustAttackThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = 0;
        for (var createdId : entry.getCreatedPermanentIds()) {
            Permanent created = gameQueryService.findPermanentById(gameData, createdId);
            if (created != null && gameQueryService.isCreature(gameData, created)) {
                created.setMustAttackThisCombat(true);
                count++;
            }
        }
        log.info("Game {} - {} created creature(s) must attack this combat if able",
                gameData.id, count);
    }
}
