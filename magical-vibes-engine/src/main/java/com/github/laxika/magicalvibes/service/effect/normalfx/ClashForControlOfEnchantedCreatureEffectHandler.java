package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashForControlOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
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
        var clash = (ClashForControlOfEnchantedCreatureEffect) effect;
        if (clash.recipientId() != null) {
            Permanent enchanted = gameQueryService.findPermanentById(gameData, clash.enchantedId());
            if (enchanted != null) {
                creatureControlService.applyControlEffect(gameData, clash.recipientId(), enchanted,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, entry.getCard().getName());
            }
            return;
        }
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        UUID enchantedId = aura == null ? null : aura.getAttachedTo();

        UUID controllerId = entry.getControllerId();
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst().orElse(null);

        boolean won = triggerCollectionService.performClash(gameData, controllerId);

        UUID newControllerId = won ? controllerId : opponentId;
        java.util.List<CardEffect> followUps = new java.util.ArrayList<>();
        followUps.add(new com.github.laxika.magicalvibes.model.effect.ScryEffect(
                1, com.github.laxika.magicalvibes.model.effect.LibraryOwner.CONTROLLER, false));
        followUps.add(new com.github.laxika.magicalvibes.model.effect.ScryEffect(
                1, com.github.laxika.magicalvibes.model.effect.LibraryOwner.OPPONENT, false));
        if (newControllerId != null && enchantedId != null) {
            followUps.add(new ClashForControlOfEnchantedCreatureEffect(enchantedId, newControllerId));
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUps);
    }
}
