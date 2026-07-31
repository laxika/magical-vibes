package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToLastRedSpellDamagerEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealDamageToLastRedSpellDamagerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToLastRedSpellDamagerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToLastRedSpellDamagerEffect) effect;
        UUID damagerId = gameData.lastRedSpellDamagerThisTurn.get(entry.getControllerId());
        if (damagerId == null || !gameData.playerLifeTotals.containsKey(damagerId)) {
            log.info("Game {} - no red spell damaged the controller this turn; {} does nothing",
                    gameData.id, entry.getCard().getName());
            return;
        }
        damageSupport.dealDamageToPlayer(gameData, entry, damagerId, e.amount());
    }
}
