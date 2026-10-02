package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.List;
import java.util.UUID;

/** Resolves the mandatory attachment choice for Auras selected by a library effect. */
public record AttachSelectedAurasToLegalTargetsFollowUp() implements LibrarySelectionFollowUp {

    @Override
    public CardEffect createEffect(List<UUID> selectedPermanentIds) {
        return new AttachSelectedAurasToLegalTargetsEffect(selectedPermanentIds);
    }

    @Override
    public String prompt() {
        return "Choose legal permanents or players for the selected Auras to enchant.";
    }

    @Override
    public boolean shouldOffer(GameData gameData, List<UUID> selectedPermanentIds) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .anyMatch(permanent -> selectedPermanentIds.contains(permanent.getId())
                        && permanent.getCard().isAura());
    }

    @Override
    public boolean optional() {
        return false;
    }
}
