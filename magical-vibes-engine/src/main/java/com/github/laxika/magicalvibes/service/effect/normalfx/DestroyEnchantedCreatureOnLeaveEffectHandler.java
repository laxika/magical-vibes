package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.UUID;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Destroys the creature an Aura was attached to as it last existed on the battlefield (the entry's source snapshot). */
@Component
@RequiredArgsConstructor
public class DestroyEnchantedCreatureOnLeaveEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEnchantedCreatureOnLeaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyEnchantedCreatureOnLeaveEffect e = (DestroyEnchantedCreatureOnLeaveEffect) effect;
        // The creature the Aura was attached to as it last existed on the battlefield.
        UUID enchantedPermanentId = entry.getSourcePermanentSnapshot() == null
                ? null : entry.getSourcePermanentSnapshot().getAttachedTo();
        if (enchantedPermanentId == null) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchantedCreature == null) {
            return;
        }

        destructionSupport.tryDestroyAndLog(
                gameData, enchantedCreature, entry.getCard().getName(), e.cannotBeRegenerated());
    }
}
