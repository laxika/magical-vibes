package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseDwarfAndAttachAnyNumberOfControlledEquipmentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseDwarfAndAttachAnyNumberOfControlledEquipmentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final AttachAnyNumberOfControlledEquipmentToTargetCreatureEffectHandler
            attachAnyNumberOfControlledEquipmentHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseDwarfAndAttachAnyNumberOfControlledEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> dwarfIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.DWARF)) {
                dwarfIds.add(permanent.getId());
            }
        }
        if (dwarfIds.isEmpty()) {
            return;
        }

        PermanentChoiceContext.ChooseDwarfAndAttachAnyNumberOfControlledEquipment context =
                new PermanentChoiceContext.ChooseDwarfAndAttachAnyNumberOfControlledEquipment(
                        entry.getCard().getName());
        playerInputService.beginPermanentChoice(
                gameData,
                entry.getControllerId(),
                dwarfIds,
                context,
                entry.getCard().getName() + " — Choose a Dwarf you control.");
    }

    public void completeChoice(GameData gameData, UUID controllerId, UUID dwarfId,
                               PermanentChoiceContext.ChooseDwarfAndAttachAnyNumberOfControlledEquipment context) {
        Permanent dwarf = gameQueryService.findPermanentById(gameData, dwarfId);
        if (dwarf == null
                || !gameQueryService.isCreature(gameData, dwarf)
                || !gameQueryService.hasEffectiveSubtype(gameData, dwarf, CardSubtype.DWARF)
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, dwarf.getId()))) {
            return;
        }

        attachAnyNumberOfControlledEquipmentHandler.beginChoiceForCreature(
                gameData, controllerId, dwarf, context.sourceCardName());
    }
}
