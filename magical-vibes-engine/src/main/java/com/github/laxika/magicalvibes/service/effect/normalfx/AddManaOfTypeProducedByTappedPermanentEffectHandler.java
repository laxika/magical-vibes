package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddManaOfTypeProducedByTappedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaOfTypeProducedByTappedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getProducedManaColors().size() != 1) {
            return;
        }
        ManaColor manaColor = entry.getProducedManaColors().iterator().next();

        AddManaOfTypeProducedByTappedPermanentEffect trigger =
                (AddManaOfTypeProducedByTappedPermanentEffect) effect;
        if (trigger.requiredColor() != null && trigger.requiredColor() != manaColor) {
            return;
        }

        // "That player adds": the player who tapped the permanent, the entry's triggering player when it names one
        // (Overabundance's and Barbflare Gremlin's abilities, controlled by their source's controller), else the
        // entry's controller (the tapping player, for the abilities that add mana to their controller).
        UUID recipientId = entry.getTriggeringPlayerId() != null ? entry.getTriggeringPlayerId() : entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (pool == null) {
            return;
        }

        pool.add(manaColor);
        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds 1 " + manaColor.getCode() + "."));
        log.info("Game {} - {} adds 1 {}", gameData.id, playerName, manaColor);
    }
}
