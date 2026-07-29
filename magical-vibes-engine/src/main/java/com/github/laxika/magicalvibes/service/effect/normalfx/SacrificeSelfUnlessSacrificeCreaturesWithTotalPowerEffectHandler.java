package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffect} (Phyrexian
 * Dreadnought). When the controller's other creatures cannot reach the required total power the
 * source is sacrificed straight away; otherwise they are prompted to pick any number of those
 * creatures. The selection is validated and carried out in {@code MultiPermanentChoiceHandlerService}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        int availablePower = 0;
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.getId().equals(sourcePermanentId) || !gameQueryService.isCreature(gameData, perm)) {
                    continue;
                }
                eligibleIds.add(perm.getId());
                availablePower += Math.max(0, gameQueryService.getEffectivePower(gameData, perm));
            }
        }

        if (availablePower < e.requiredPower()) {
            sacrificeSource(gameData, controllerId, sourcePermanentId);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                new MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource(
                        sourcePermanentId, e.requiredPower()),
                "Sacrifice any number of creatures with total power " + e.requiredPower()
                        + " or greater, or choose none to sacrifice " + entry.getCard().getName() + ".");
    }

    private void sacrificeSource(GameData gameData, UUID controllerId, UUID sourcePermanentId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            destructionSupport.sacrificeAndLog(gameData, source, controllerId);
        }
    }
}
