package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureFightsTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureFightsTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID auraId = entry.getSourcePermanentId();
        if (targetId == null || auraId == null) {
            return;
        }

        // The fighting creature is the one this Aura is attached to (the enchanted creature), not the
        // Aura permanent itself. Re-derive it from the attachment at resolution.
        // An Aura that has left the battlefield uses what it was last attached to (CR 608.2h).
        Permanent aura = gameQueryService.findPermanentById(gameData, auraId);
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || aura.getAttachedTo() == null) {
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (enchanted == null || target == null) {
            return;
        }

        fightSupport.fight(gameData, entry, enchanted, target);
    }
}
