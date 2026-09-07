package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.LandEquilibriumEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Applies Land Equilibrium replacement effects when a land enters the battlefield.
 */
@Service
public class LandEquilibriumSupport {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    public LandEquilibriumSupport(GameQueryService gameQueryService,
                                  PlayerInputService playerInputService,
                                  @Lazy DestructionSupport destructionSupport) {
        this.gameQueryService = gameQueryService;
        this.playerInputService = playerInputService;
        this.destructionSupport = destructionSupport;
    }

    public ReplacementPlan findPlan(GameData gameData, UUID enteringControllerId, Permanent entering) {
        if (!gameQueryService.isLand(gameData, entering)) {
            return null;
        }

        int enteringControllerLandCount = countControlledLands(gameData, enteringControllerId);
        int replacementCount = 0;
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            UUID sourceControllerId = entry.getKey();
            if (sourceControllerId.equals(enteringControllerId)
                    || enteringControllerLandCount < countControlledLands(gameData, sourceControllerId)) {
                continue;
            }
            for (Permanent permanent : entry.getValue()) {
                replacementCount += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(LandEquilibriumEffect.class::isInstance)
                        .count();
            }
        }
        return replacementCount == 0 ? null : new ReplacementPlan(replacementCount);
    }

    public void applyPlan(GameData gameData, UUID enteringControllerId, Permanent entering,
                           ReplacementPlan plan, Zone landPlayZone) {
        if (plan == null) {
            return;
        }
        beginNext(gameData, enteringControllerId, entering.getCard(), landPlayZone,
                plan.replacementCount());
    }

    public boolean beginNext(GameData gameData, UUID sacrificingPlayerId, Card enteringCard,
                             Zone landPlayZone, int remainingReplacements) {
        if (remainingReplacements <= 0) {
            return false;
        }

        List<Permanent> lands = controlledLands(gameData, sacrificingPlayerId);
        if (lands.isEmpty()) {
            return false;
        }
        if (lands.size() == 1) {
            destructionSupport.sacrificeAndLog(gameData, lands.getFirst(), sacrificingPlayerId);
            return beginNext(gameData, sacrificingPlayerId, enteringCard, landPlayZone,
                    remainingReplacements - 1);
        }

        List<UUID> landIds = lands.stream().map(Permanent::getId).toList();
        playerInputService.beginPermanentChoice(gameData, sacrificingPlayerId, landIds,
                new PermanentChoiceContext.LandEquilibriumSacrifice(
                        sacrificingPlayerId, enteringCard, landPlayZone, remainingReplacements),
                "Land Equilibrium — Choose a land to sacrifice.");
        return true;
    }

    private List<Permanent> controlledLands(GameData gameData, UUID playerId) {
        List<Permanent> lands = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (gameQueryService.isLand(gameData, permanent)) {
                lands.add(permanent);
            }
        }
        return lands;
    }

    private int countControlledLands(GameData gameData, UUID playerId) {
        return controlledLands(gameData, playerId).size();
    }

    public record ReplacementPlan(int replacementCount) {
    }
}
