package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.List;
import java.util.UUID;

/** Lets the controller optionally attach an Equipment selected by a library effect. */
public record AttachSelectedEquipmentToCreatureEffect(List<UUID> equipmentPermanentIds)
        implements CardEffect, LibrarySelectionFollowUp {

    public AttachSelectedEquipmentToCreatureEffect() {
        this(List.of());
    }

    public AttachSelectedEquipmentToCreatureEffect {
        equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
    }

    @Override
    public CardEffect createEffect(List<UUID> selectedPermanentIds) {
        return new AttachSelectedEquipmentToCreatureEffect(selectedPermanentIds);
    }

    @Override
    public String prompt() {
        return "Attach that Equipment to a creature you control?";
    }

    @Override
    public boolean shouldOffer(GameData gameData, List<UUID> selectedPermanentIds) {
        return selectedPermanentIds.size() == 1
                && gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getId().equals(selectedPermanentIds.getFirst()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT));
    }
}
