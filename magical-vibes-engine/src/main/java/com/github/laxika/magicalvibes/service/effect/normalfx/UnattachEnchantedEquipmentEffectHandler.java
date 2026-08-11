package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link UnattachEnchantedEquipmentEffect} against the source Aura's current attachment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnattachEnchantedEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnattachEnchantedEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = findSourceAura(gameData, entry);
        if (aura == null || aura.getAttachedTo() == null) {
            return;
        }

        Permanent equipment = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (equipment == null || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || equipment.getAttachedTo() == null) {
            return;
        }

        var equippedPermanentId = equipment.getAttachedTo();
        equipment.setAttachedTo(null);
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" unattaches ")
                .card(equipment.getCard())
                .build());
        log.info("Game {} - {} unattaches enchanted Equipment {}", gameData.id,
                entry.getCard().getName(), equipment.getCard().getName());

        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, equippedPermanentId, null);
    }

    private Permanent findSourceAura(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null && source.getCard().getId().equals(entry.getCard().getId())) {
            return source;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
