package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/** Attaches one of the Equipment permanents selected by a library effect to a creature the controller controls. */
public record AttachOneOfEquipmentToCreatureEffect()
        implements CardEffect, LibrarySelectionFollowUp {

    @Override
    public CardEffect effect() {
        return this;
    }

    @Override
    public String prompt() {
        return "Attach one of those Equipment to a creature you control?";
    }

    @Override
    public boolean shouldOffer(GameData gameData, List<UUID> selectedPermanentIds) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> selectedPermanentIds.contains(permanent.getId()))
                .map(Permanent::getCard)
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.EQUIPMENT));
    }
}
