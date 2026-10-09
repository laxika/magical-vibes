package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinDoubleOrPreventNextDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link FlipCoinDoubleOrPreventNextDamageFromChosenSourceEffect}: offers controlled
 * permanents, spells, and sources of pending abilities as damage sources, then hands off to
 * {@code PermanentChoiceBattlefieldHandlerService}, which flips the coin and installs the doubling or
 * prevention shield.
 */
@Component
@RequiredArgsConstructor
public class FlipCoinDoubleOrPreventNextDamageFromChosenSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinDoubleOrPreventNextDamageFromChosenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(controllerId)) {
                validIds.add(perm.getId());
            }
        });
        for (StackEntry stackEntry : gameData.stack) {
            boolean ability = stackEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                    || stackEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
            UUID sourceController = ability && stackEntry.getDamageSourceControllerId() != null
                    ? stackEntry.getDamageSourceControllerId() : stackEntry.getControllerId();
            UUID sourceId = ability ? stackEntry.getSourcePermanentId() : stackEntry.getTargetableId();
            if (sourceId == null && ability && stackEntry.getSourcePermanentSnapshot() != null) {
                sourceId = stackEntry.getSourcePermanentSnapshot().getId();
            }
            if (controllerId.equals(sourceController) && sourceId != null && !validIds.contains(sourceId)) {
                validIds.add(sourceId);
            }
        }
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DoubleOrPreventNextDamageFromSourceChoice(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source you control and flip a coin. If you win the flip, the next time that source"
                        + " would deal damage this turn, it deals double that damage instead. If you lose, prevent"
                        + " that damage instead.");
    }
}
