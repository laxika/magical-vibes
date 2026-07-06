package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEachCreatureDamagedPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachCreatureDamagedPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null) return;

        int damageDealt = entry.getXValue();
        if (damageDealt <= 0) return;

        int damage = gameQueryService.applyDamageMultiplier(gameData, damageDealt, entry);
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (battlefield != null) {
            List<Permanent> destroyed = new ArrayList<>();
            for (Permanent creature : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, creature)) continue;
                if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, creature, entry.getCard())) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            cardName + "'s damage to " + creature.getCard().getName() + " is prevented.");
                    continue;
                }
                if (damageSupport.dealCreatureDamage(gameData, entry, creature, damage)) {
                    destroyed.add(creature);
                }
            }
            damageSupport.destroyAllLethal(gameData, destroyed);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
