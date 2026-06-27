package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SourceFightsTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceFightsTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;

        String cardName = entry.getCard().getName();

        // Determine source power: use effective power if on battlefield, else fall back to card base power.
        // Clamped to 0 per CR — creatures with 0 or negative power deal no damage.
        int sourcePower;
        if (source != null) {
            sourcePower = gameQueryService.getPowerBasedDamage(gameData, source);
        } else {
            sourcePower = Math.max(0, entry.getCard().getPower() != null ? entry.getCard().getPower() : 0);
        }

        // Source deals damage equal to its power to target
        if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, target, entry.getCard()))) {
            int sourceDamage = gameQueryService.applyDamageMultiplier(gameData, sourcePower, entry);
            if (damageSupport.dealCreatureDamage(gameData, entry, target, sourceDamage)) {
                gameData.pendingLethalDamageDestructions.add(target);
            }
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + " deals " + sourceDamage + " damage to " + target.getCard().getName() + ".");
        } else {
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s damage to " + target.getCard().getName() + " is prevented.");
        }

        // Target deals damage equal to its power back to source (only if source is still on the battlefield)
        if (source != null) {
            int targetPower = gameQueryService.getPowerBasedDamage(gameData, target);
            if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, source, target.getCard()))) {
                int targetDamage = gameQueryService.applyDamageMultiplier(gameData, targetPower, entry);
                if (damageSupport.dealCreatureDamage(gameData, entry, source, targetDamage, target)) {
                    gameData.pendingLethalDamageDestructions.add(source);
                }
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " deals " + targetDamage + " damage to " + cardName + ".");
            } else {
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + "'s damage to " + cardName + " is prevented.");
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
