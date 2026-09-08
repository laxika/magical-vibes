package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombustibleGearhulkEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Starts Combustible Gearhulk's opponent-choice resolution. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CombustibleGearhulkEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CombustibleGearhulkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID opponentId = entry.getTargetId();
        if (opponentId == null) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(entry.getControllerId());
        String prompt = "Have " + controllerName + " draw three cards? If not, they mill three cards "
                + "and " + entry.getCard().getName() + " deals damage to you equal to their total mana value.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), opponentId, List.of(effect), prompt,
                opponentId, null, entry.getSourcePermanentId(), null, 0, 0,
                null, null, null, null, entry.getControllerId(), null));
        playerInputService.processNextMayAbility(gameData);

        log.info("Game {} - {} asks {} to choose the Combustible Gearhulk mode",
                gameData.id, entry.getCard().getName(), gameData.playerIdToName.get(opponentId));
    }
}
