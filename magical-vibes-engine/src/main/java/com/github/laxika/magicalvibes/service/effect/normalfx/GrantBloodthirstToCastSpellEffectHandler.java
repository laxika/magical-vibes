package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantBloodthirstToCastSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves "it gains bloodthirst N" by stamping the grant on the triggering spell's stack entry
 * (Bloodlord of Vaasgoth). The counters themselves are applied later by the as-enters replacement
 * in {@code BattlefieldEntryService}, which is what makes the grant a real static ability
 * (CR 702.54a) rather than an after-the-fact counter placement.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrantBloodthirstToCastSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantBloodthirstToCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantBloodthirstToCastSpellEffect) effect;
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null) {
            return;
        }

        for (StackEntry spellEntry : gameData.stack) {
            if (spellEntry.getCard() != null && spellCardId.equals(spellEntry.getCard().getId())) {
                // Each instance of bloodthirst applies separately (CR 702.54c), so grants accumulate.
                spellEntry.setGrantedBloodthirst(spellEntry.getGrantedBloodthirst() + grant.amount());
                gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                        " gains bloodthirst " + grant.amount() + "."));
                log.info("Game {} - {} gains bloodthirst {}",
                        gameData.id, spellEntry.getCard().getName(), grant.amount());
                return;
            }
        }
    }
}
