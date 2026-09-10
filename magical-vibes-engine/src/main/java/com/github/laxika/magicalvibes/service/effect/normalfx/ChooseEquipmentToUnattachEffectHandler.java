package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseEquipmentToUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentIfAttachedToControlledCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseEquipmentToUnattachEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final UnattachEquipmentIfAttachedToControlledCreatureEffectHandler unattachHandler;
    private final TapUntapSupport tapUntapSupport;
    private final GrantKeywordEffectHandler grantKeywordEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseEquipmentToUnattachEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> equipmentIds = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent equipment : battlefield) {
                if (!GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                        || equipment.getAttachedTo() == null) {
                    continue;
                }

                Permanent creature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
                if (creature != null
                        && gameQueryService.isCreature(gameData, creature)
                        && Objects.equals(entry.getControllerId(),
                        gameQueryService.findPermanentController(gameData, creature.getId()))) {
                    equipmentIds.add(equipment.getId());
                }
            }
        }

        if (!equipmentIds.isEmpty()) {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    entry.getControllerId(),
                    equipmentIds,
                    1,
                    new MultiPermanentChoiceContext.UnattachEquipmentFromControlledCreature(entry),
                    entry.getCard().getName() + " — Choose an Equipment to unattach, or choose none.");
        }
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.UnattachEquipmentFromControlledCreature context) {
        if (permanentIds.isEmpty()) {
            return;
        }

        UUID equipmentId = permanentIds.get(0);
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null
                || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || equipment.getAttachedTo() == null) {
            return;
        }

        UUID creatureId = equipment.getAttachedTo();
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        StackEntry resolvingEntry = context.resolvingEntry();
        if (creature == null
                || !gameQueryService.isCreature(gameData, creature)
                || !Objects.equals(resolvingEntry.getControllerId(),
                gameQueryService.findPermanentController(gameData, creatureId))) {
            return;
        }

        unattachHandler.resolve(gameData, resolvingEntry,
                new UnattachEquipmentIfAttachedToControlledCreatureEffect(equipmentId));

        Permanent remainingCreature = gameQueryService.findPermanentById(gameData, creatureId);
        if (remainingCreature == null
                || !gameQueryService.isCreature(gameData, remainingCreature)
                || !Objects.equals(resolvingEntry.getControllerId(),
                gameQueryService.findPermanentController(gameData, creatureId))) {
            return;
        }

        tapUntapSupport.tapPermanent(gameData, remainingCreature, resolvingEntry.getControllerId());
        grantKeywordEffectHandler.grantToPermanent(gameData, resolvingEntry, remainingCreature,
                Set.of(Keyword.INDESTRUCTIBLE));
    }
}
