package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChangeTargetOfTargetSpellToEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TargetRedirectionSupport targetRedirectionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeTargetOfTargetSpellToEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, triggeringCardId);
        if (targetSpell == null || !targetSpell.isSingleTarget()) {
            return;
        }

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        UUID enchantedCreatureId = aura == null ? entry.getTargetId() : aura.getAttachedTo();
        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedCreatureId);
        if (enchantedCreature == null || !gameQueryService.isCreature(gameData, enchantedCreature)) {
            return;
        }
        if (enchantedCreatureId.equals(targetSpell.getTargetId())) {
            return;
        }

        if (targetRedirectionSupport.isValidNewTargetForSpell(gameData, targetSpell, enchantedCreatureId)) {
            targetSpell.setTargetId(enchantedCreatureId);
            for (int i = 0; i < targetSpell.getDeclaredTargetIds().size(); i++) {
                targetSpell.replaceTargetIdAt(i, enchantedCreatureId);
            }
        }
    }
}
