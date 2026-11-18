package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.RegenerationShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
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
                if (regenerationTargetId == null && entry.targetsForEffect(effect).size() == 1) {
                    regenerationTargetId = entry.targetsForEffect(effect).getFirst();
                }
                if (effect instanceof RegenerateEffect chosenOpponentRegenerate
                        && chosenOpponentRegenerate.opponentMayDrawOnRegenerate()
                        && !chosenOpponentRegenerate.targetsPermanent()
                        && regenerationTargetId != null && gameData.playerIds.contains(regenerationTargetId)) {
                    // The target is the opponent who may draw; the creature regenerating is the source.
                    regenerationTargetId = entry.getSourcePermanentId();
                }
                if (entry.getSourcePermanentId() != null) {
                    Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                    if (source != null && source.getAttachedTo() != null) {
                        regenerationTargetId = source.getAttachedTo();
                    } else if (source != null
                            && source.getCard().getSubtypes().contains(
                            com.github.laxika.magicalvibes.model.CardSubtype.EQUIPMENT)
                            && !source.getId().equals(regenerationTargetId)) {
                        // "Regenerate equipped creature" while the Equipment is no longer attached: there is
                        // no equipped creature to regenerate.
                        return;
                    } else if (source == null && entry.getSourcePermanentSnapshot() != null
                            && entry.getSourcePermanentSnapshot().getAttachedTo() != null) {
                        // The enchanted creature is the source's last known information once the Aura has
                        // left the battlefield (CR 608.2h, CR 113.7a).
                        regenerationTargetId = entry.getSourcePermanentSnapshot().getAttachedTo();
                    }
                }

                Permanent perm = gameQueryService.findPermanentById(gameData, regenerationTargetId);
                if (perm == null) {
                    return;
                }
                gameData.addEffectOnPermanent(entry, shieldFor(gameData, entry, effect), perm.getId(),
                        Duration.UNTIL_END_OF_TURN);

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " gains a regeneration shield."));
                log.info("Game {} - {} gains a regeneration shield", gameData.id, perm.getCard().getName());
    
    }

    /**
     * The regeneration shield the resolving {@code entry} makes (CR 701.19a), with the rider its effect names. Soldevi
     * Sentry's draw goes to the chosen opponent, or to the controller's opponent; with no opponent it has no rider.
     */
    private RegenerationShield shieldFor(GameData gameData, StackEntry entry, CardEffect effect) {
        if (!(effect instanceof RegenerateEffect regenerate)) {
            return RegenerationShield.PLAIN;
        }
        if (regenerate.opponentMayDrawOnRegenerate()) {
            UUID chosenOpponentId = !regenerate.targetsPermanent()
                    && entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                    ? entry.getTargetId() : null;
            UUID opponentId = chosenOpponentId != null ? chosenOpponentId
                    : gameQueryService.getOpponentId(gameData, entry.getControllerId());
            return opponentId == null ? RegenerationShield.PLAIN
                    : new RegenerationShield(RegenerationShield.Rider.OPPONENT_DRAW, opponentId);
        }
        if (regenerate.putMinusOneCounterOnRegenerate()) {
            return new RegenerationShield(RegenerationShield.Rider.MINUS_ONE_COUNTER, null);
        }
        if (regenerate.putPlusOnePlusOneCounterOnRegenerate()) {
            return new RegenerationShield(RegenerationShield.Rider.PLUS_ONE_COUNTER, null);
        }
        if (regenerate.gainControlOnRegenerate() && entry.getControllerId() != null) {
            return new RegenerationShield(RegenerationShield.Rider.GAIN_CONTROL, null);
        }
        return RegenerationShield.PLAIN;
    }
}
