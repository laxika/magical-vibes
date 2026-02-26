package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.service.GameQueryService;

import java.util.List;
import java.util.UUID;

public class ArtifactSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final SacrificeArtifactCost cost;
    private final GameQueryService gameQueryService;
    private final PermanentSacrificeAction sacrificeAction;

    public ArtifactSacrificeCostHandler(SacrificeArtifactCost cost, GameQueryService gameQueryService, PermanentSacrificeAction sacrificeAction) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.sacrificeAction = sacrificeAction;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 1; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No artifact to sacrifice");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(gameQueryService::isArtifact)
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isArtifact(chosen)) {
            throw new IllegalStateException("Must sacrifice an artifact");
        }
        sacrificeAction.sacrifice(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an artifact to sacrifice.";
    }
}
