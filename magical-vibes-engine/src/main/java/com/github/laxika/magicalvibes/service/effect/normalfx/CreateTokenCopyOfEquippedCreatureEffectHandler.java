package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CreateTokenCopyOfEquippedCreatureEffect} through the shared token-copy pipeline so
 * copiable values (colors, supertypes, abilities) are copied exactly and granted haste stays a
 * non-copiable grant. When the equipped creature has left the battlefield, its last known information
 * is copied.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEquippedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEquippedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfEquippedCreatureEffect) effect;

        UUID equipmentPermanentId = e.equipmentPermanentId() != null
                ? e.equipmentPermanentId()
                : entry.getSourcePermanentId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, equipmentPermanentId);
        if (sourcePermanent == null) {
            sourcePermanent = entry.getSourcePermanentSnapshot();
        }
        if (sourcePermanent == null) {
            log.info("Game {} - Source equipment no longer on battlefield", gameData.id);
            return;
        }

        UUID equippedCreatureId = sourcePermanent.getAttachedTo();
        if (equippedCreatureId == null) {
            log.info("Game {} - Equipment is not attached to any creature", gameData.id);
            return;
        }

        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equippedCreatureId);
        if (equippedCreature == null) {
            equippedCreature = gameData.departedPermanentSnapshots.values().stream()
                    .filter(departed -> equippedCreatureId.equals(departed.getId()))
                    .findFirst()
                    .orElse(null);
        }
        if (equippedCreature == null) {
            log.info("Game {} - Equipped creature is no longer available", gameData.id);
            return;
        }
        if (e.amount() <= 0) {
            return;
        }

        CreateTokenCopyOfTargetPermanentEffect copyEffect = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), e.grantHaste(), false, false, false, false, false,
                null, Set.of(), e.removeLegendary());
        tokenCopySupport.createTokenCopies(gameData, entry,
                Collections.nCopies(e.amount(), equippedCreature.getCard()), equippedCreature, copyEffect);
    }
}
