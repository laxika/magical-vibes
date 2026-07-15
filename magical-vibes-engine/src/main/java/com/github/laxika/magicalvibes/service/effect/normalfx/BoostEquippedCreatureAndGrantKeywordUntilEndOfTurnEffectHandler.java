package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect) effect;
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "Equipment";

        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment == null || equipment.getAttachedTo() == null) {
            log.info("Game {} - {} trigger fizzles: equipment no longer attached", gameData.id, sourceName);
            return;
        }
        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equippedCreature == null) {
            log.info("Game {} - {} trigger fizzles: equipped creature no longer on battlefield", gameData.id, sourceName);
            return;
        }

        equippedCreature.setPowerModifier(equippedCreature.getPowerModifier() + e.powerBoost());
        equippedCreature.setToughnessModifier(equippedCreature.getToughnessModifier() + e.toughnessBoost());
        equippedCreature.getGrantedKeywords().add(e.keyword());

        String keywordName = e.keyword().name().charAt(0)
                + e.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = equippedCreature.getCard().getName() + " gets +" + e.powerBoost() + "/+"
                + e.toughnessBoost() + " and gains " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets +{}/+{} and gains {} until end of turn", gameData.id,
                equippedCreature.getCard().getName(), e.powerBoost(), e.toughnessBoost(), e.keyword());
    }
}
