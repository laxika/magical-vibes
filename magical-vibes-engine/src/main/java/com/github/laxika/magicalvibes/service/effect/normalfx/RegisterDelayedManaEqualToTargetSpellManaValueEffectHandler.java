package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaValueEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link RegisterDelayedManaEqualToTargetSpellManaValueEffect} (Scattering Stroke's clash-win
 * reward). Reads the mana value of the targeted spell — still on the stack because the reward resolves
 * before the counter — and registers an {@link AddManaAtNextMainPhase} delayed trigger for the
 * controller. If the spell already left the stack (or has mana value 0) nothing is scheduled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedManaEqualToTargetSpellManaValueEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedManaEqualToTargetSpellManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedManaEqualToTargetSpellManaValueEffect) effect;

        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }
        if (targetEntry == null) {
            log.info("Game {} - Clash reward target no longer on stack, no delayed mana scheduled", gameData.id);
            return;
        }

        int manaValue = targetEntry.getCard().getManaValue() + targetEntry.getXValue();
        if (manaValue <= 0) return;

        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(
                new AddManaAtNextMainPhase(controllerId, e.color(), manaValue, entry.getCard()));

        log.info("Game {} - {} schedules {} {} at their next main phase (clash win)",
                gameData.id, gameData.playerIdToName.get(controllerId), manaValue, e.color());
    }
}
