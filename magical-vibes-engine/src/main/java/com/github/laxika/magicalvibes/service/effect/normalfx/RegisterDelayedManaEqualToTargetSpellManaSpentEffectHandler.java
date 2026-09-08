package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaSpentEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a delayed-mana reward based on a targeted spell's actual cast payment. */
@Slf4j
@Component
public class RegisterDelayedManaEqualToTargetSpellManaSpentEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedManaEqualToTargetSpellManaSpentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }

        StackEntry targetEntry = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetCardId))
                .findFirst()
                .orElse(null);
        if (targetEntry == null || targetEntry.getManaSpentToCast() <= 0) {
            return;
        }

        var delayedMana = (RegisterDelayedManaEqualToTargetSpellManaSpentEffect) effect;
        gameData.queueDelayedAction(new AddManaAtNextMainPhase(
                entry.getControllerId(), delayedMana.color(), targetEntry.getManaSpentToCast(), entry.getCard(), false, false));

        log.info("Game {} - {} schedules {} {} at their next main phase (mana spent)",
                gameData.id,
                gameData.playerIdToName.get(entry.getControllerId()),
                targetEntry.getManaSpentToCast(),
                delayedMana.color());
    }
}
