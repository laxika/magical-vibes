package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves mana production from a color chosen earlier in the same resolution. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaOfChosenColorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfChosenColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CardColor chosenColor = gameData.chosenSpellColor;
        gameData.chosenSpellColor = null;
        if (chosenColor == null) {
            return;
        }

        ManaColor manaColor = ManaColor.valueOf(chosenColor.name());
        manaColor = ManaProductionSupport.effectiveColor(gameData, entry.getControllerId(), manaColor);
        ManaPool pool = gameData.playerManaPools.get(entry.getControllerId());
        pool.add(manaColor);

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source != null && gameQueryService.isCreature(gameData, source)) {
            pool.addCreatureMana(manaColor, 1);
        }
        if (source != null && entry.getSourcePermanentId() != null
                && gameQueryService.isLand(gameData, source)) {
            triggerCollectionService.checkLandTapTriggers(gameData, entry.getControllerId(),
                    entry.getSourcePermanentId());
        }

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds 1 " + manaColor.getCode() + "."));
        log.info("Game {} - {} adds 1 {}", gameData.id, playerName, manaColor);
    }
}
