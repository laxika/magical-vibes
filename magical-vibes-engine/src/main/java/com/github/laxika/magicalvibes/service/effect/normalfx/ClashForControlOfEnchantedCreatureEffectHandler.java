package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashForControlOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link ClashForControlOfEnchantedCreatureEffect} (Captivating Glance): clashes with an
 * opponent for the aura's controller, then gives control of the enchanted creature to the winner —
 * the controller on a win, otherwise the clash opponent.
 */
@Component
@RequiredArgsConstructor
public class ClashForControlOfEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final TriggerCollectionService triggerCollectionService;
    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ClashForControlOfEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchantedCreature == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst().orElse(null);

        boolean won = triggerCollectionService.performClash(gameData, controllerId);

        UUID newControllerId = won ? controllerId : opponentId;
        if (newControllerId != null) {
            creatureControlService.stealPermanent(gameData, newControllerId, enchantedCreature);
            // Indefinite control change (no stated duration) — never reverted at end of turn.
            gameData.permanentControlStolenCreatures.add(enchantedCreature.getId());
        }
    }
}
