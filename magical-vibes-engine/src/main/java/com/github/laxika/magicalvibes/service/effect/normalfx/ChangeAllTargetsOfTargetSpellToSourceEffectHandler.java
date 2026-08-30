package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeAllTargetsOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChangeAllTargetsOfTargetSpellToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TargetRedirectionSupport targetRedirectionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeAllTargetsOfTargetSpellToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSpell == null || entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null || !gameQueryService.isCreature(gameData, sourcePermanent)) {
            return;
        }

        List<UUID> targetIds = targetOccurrences(targetSpell);
        if (targetIds.isEmpty() || targetIds.stream().distinct().count() != 1) {
            return;
        }

        Permanent currentTarget = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        if (currentTarget == null || !gameQueryService.isCreature(gameData, currentTarget)
                || !targetRedirectionSupport.isValidNewTargetForSpell(gameData, targetSpell, sourcePermanent.getId())) {
            return;
        }

        if (targetSpell.getTargetId() != null) {
            targetSpell.setTargetId(sourcePermanent.getId());
        }
        for (int i = 0; i < targetSpell.getDeclaredTargetIds().size(); i++) {
            targetSpell.replaceTargetIdAt(i, sourcePermanent.getId());
        }
    }

    private List<UUID> targetOccurrences(StackEntry targetSpell) {
        List<UUID> targetIds = new ArrayList<>();
        if (targetSpell.getTargetId() != null) {
            targetIds.add(targetSpell.getTargetId());
        }
        targetIds.addAll(targetSpell.getDeclaredTargetIds());
        return targetIds;
    }
}
