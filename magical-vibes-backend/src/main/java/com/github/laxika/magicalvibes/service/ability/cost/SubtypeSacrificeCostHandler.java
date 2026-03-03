package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link SacrificeSubtypeCreatureCost} — the player must sacrifice one creature
 * of a specific subtype (e.g. "Sacrifice a Goblin"). Filters the battlefield to creatures
 * matching the required subtype.
 */
public class SubtypeSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final SacrificeSubtypeCreatureCost cost;
    private final GameQueryService gameQueryService;
    private final PermanentSacrificeAction sacrificeAction;

    /**
     * @param cost            the cost effect record specifying the required subtype
     * @param gameQueryService used to check creature status
     * @param sacrificeAction  callback that performs the actual sacrifice
     */
    public SubtypeSacrificeCostHandler(SacrificeSubtypeCreatureCost cost, GameQueryService gameQueryService, PermanentSacrificeAction sacrificeAction) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.sacrificeAction = sacrificeAction;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 1; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("Must choose a " + cost.subtype().getDisplayName() + " to sacrifice");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> p.getCard().getSubtypes().contains(cost.subtype()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must sacrifice a creature");
        }
        if (!chosen.getCard().getSubtypes().contains(cost.subtype())) {
            throw new IllegalStateException("Must sacrifice a " + cost.subtype().getDisplayName());
        }
        sacrificeAction.sacrifice(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a " + cost.subtype().getDisplayName() + " to sacrifice.";
    }
}
