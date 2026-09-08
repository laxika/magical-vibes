package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachOneOfEquipmentToSamuraiEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachOneOfEquipmentToSamuraiEffectHandler implements NormalEffectHandlerBean {

    private final AttachOneOfEquipmentToSamuraiSupport support;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachOneOfEquipmentToSamuraiEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AttachOneOfEquipmentToSamuraiEffect attachEffect =
                (AttachOneOfEquipmentToSamuraiEffect) effect;
        List<UUID> samuraiIds = support.legalSamuraiIds(
                gameData, entry.getControllerId(), attachEffect.equipmentPermanentIds());
        if (samuraiIds.isEmpty()) {
            return;
        }

        if (samuraiIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachEquipmentToSamurai(
                            attachEffect.equipmentPermanentIds()));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), samuraiIds,
                    entry.getCard().getName() + " - Choose a Samurai to attach an Equipment to.");
            return;
        }

        resolveForSamurai(gameData, entry.getControllerId(), samuraiIds.getFirst(),
                attachEffect.equipmentPermanentIds(), entry.getCard().getName());
    }

    public void resolveForSamurai(GameData gameData, UUID controllerId, UUID samuraiId,
                                  List<UUID> equipmentPermanentIds, String sourceName) {
        Permanent samurai = gameQueryService.findPermanentById(gameData, samuraiId);
        if (samurai == null) {
            return;
        }

        List<UUID> legalEquipmentIds = support.legalEquipmentIds(
                gameData, samurai, equipmentPermanentIds);
        if (legalEquipmentIds.isEmpty()) {
            return;
        }
        if (legalEquipmentIds.size() == 1) {
            support.attach(gameData, legalEquipmentIds.getFirst(), samuraiId);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.AttachEquipmentToSamuraiTarget(
                        samuraiId, legalEquipmentIds));
        playerInputService.beginPermanentChoice(gameData, controllerId, legalEquipmentIds,
                sourceName + " - Choose an Equipment to attach.");
    }
}
