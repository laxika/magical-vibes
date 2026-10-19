package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect) effect;
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "Equipment";

        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        // An equipment that already left the battlefield (e.g. a planeswalker-equipment whose loyalty cost
        // removed its last counter) is looked back at through the snapshot taken on activation (CR 608.2h).
        Permanent equipmentLastKnown = equipment != null ? equipment : entry.getSourcePermanentSnapshot();
        // "that creature" in an "equipped creature attacks" trigger is the creature that attacked, so the
        // creature recorded on the trigger wins over whatever the Equipment is attached to by resolution.
        UUID attachedCreatureId = entry.getTriggeringPermanentId() != null
                && !entry.getTriggeringPermanentId().equals(entry.getSourcePermanentId())
                && gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId()) != null
                ? entry.getTriggeringPermanentId()
                : equipmentLastKnown != null && equipmentLastKnown.getAttachedTo() != null
                ? equipmentLastKnown.getAttachedTo()
                : entry.getTriggeringPermanentId();
        if (attachedCreatureId == null) {
            log.info("Game {} - {} trigger fizzles: no attached creature was recorded", gameData.id, sourceName);
            return;
        }
        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, attachedCreatureId);
        if (equippedCreature == null) {
            log.info("Game {} - {} trigger fizzles: equipped creature no longer on battlefield", gameData.id, sourceName);
            return;
        }

        equippedCreature.setPowerModifier(equippedCreature.getPowerModifier() + e.powerBoost());
        equippedCreature.setToughnessModifier(equippedCreature.getToughnessModifier() + e.toughnessBoost());
        equippedCreature.getGrantedKeywords().add(e.keyword());

        String keywordName = e.keyword().name().charAt(0)
                + e.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String boostText = e.powerBoost() == 0 && e.toughnessBoost() == 0
                ? ""
                : " gets +" + e.powerBoost() + "/+" + e.toughnessBoost() + " and";
        gameLogService.append(gameData, GameLog.builder().card(equippedCreature.getCard()).text(boostText + " gains " + keywordName + " until end of turn.").build());
        log.info("Game {} - {} gets +{}/+{} and gains {} until end of turn", gameData.id,
                equippedCreature.getCard().getName(), e.powerBoost(), e.toughnessBoost(), e.keyword());
    }
}
