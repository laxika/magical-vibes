package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GargantuanGorillaUpkeepEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.GargantuanGorillaUpkeepSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accept/decline half of Gargantuan Gorilla's upkeep trigger. Accepting with several Forests starts
 * a choice; declining — or no longer controlling a Forest — sacrifices the Gorilla and deals it 7
 * damage worth to its controller.
 */
@Component
@RequiredArgsConstructor
public class GargantuanGorillaUpkeepHandler implements MayEffectHandlerBean {

    private final GargantuanGorillaUpkeepSupport gargantuanGorillaUpkeepSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GargantuanGorillaUpkeepEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID controllerId = ability.controllerId();
        UUID sourcePermanentId = ability.sourcePermanentId();

        if (accepted) {
            List<UUID> forestIds = gargantuanGorillaUpkeepSupport.forestIds(gameData, controllerId);
            if (forestIds.size() == 1) {
                gargantuanGorillaUpkeepSupport.sacrificeForest(
                        gameData, controllerId, forestIds.getFirst(), sourcePermanentId);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (forestIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.GargantuanGorillaSacrificeForest(
                                controllerId, sourcePermanentId, ability.sourceCard()));
                playerInputService.beginPermanentChoice(gameData, controllerId, forestIds,
                        "Choose a Forest to sacrifice.");
                return;
            }
            // Accepted but no Forest left — treated as not having sacrificed one.
        }

        gargantuanGorillaUpkeepSupport.applyPenalty(gameData, controllerId, sourcePermanentId, ability.sourceCard());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
