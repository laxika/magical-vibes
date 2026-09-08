package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegenerateEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegenerateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                UUID regenerationTargetId = effect.targetSpec().selfTargeting()
                        && entry.getTriggeringCardId() != null
                        && entry.getSourcePermanentId() != null
                        ? entry.getSourcePermanentId()
                        : entry.getTargetId();
                if (entry.getSourcePermanentId() != null) {
                    Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                    if (source != null && source.getAttachedTo() != null) {
                        regenerationTargetId = source.getAttachedTo();
                    }
                }

                Permanent perm = gameQueryService.findPermanentById(gameData, regenerationTargetId);
                if (perm == null) {
                    return;
                }
                perm.setRegenerationShield(perm.getRegenerationShield() + 1);
                if (effect instanceof RegenerateEffect regenerate) {
                    if (regenerate.opponentMayDrawOnRegenerate()) {
                        perm.setOpponentDrawRegenerationShield(perm.getOpponentDrawRegenerationShield() + 1);
                        UUID opponentId = gameQueryService.getOpponentId(gameData, entry.getControllerId());
                        if (opponentId != null) {
                            perm.getOpponentDrawRegenerationShieldRecipients().add(opponentId);
                        }
                    }
                    if (regenerate.putMinusOneCounterOnRegenerate()) {
                        perm.setMinusOneCounterRegenerationShield(perm.getMinusOneCounterRegenerationShield() + 1);
                    }
                    if (regenerate.putPlusOnePlusOneCounterOnRegenerate()) {
                        perm.setPlusOnePlusOneCounterRegenerationShield(
                                perm.getPlusOnePlusOneCounterRegenerationShield() + 1);
                    }
                    if (regenerate.gainControlOnRegenerate() && entry.getControllerId() != null) {
                        perm.getGainControlRegenerationShields().add(entry.getControllerId());
                    }
                }

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " gains a regeneration shield."));
                log.info("Game {} - {} gains a regeneration shield", gameData.id, perm.getCard().getName());
    
    }
}
