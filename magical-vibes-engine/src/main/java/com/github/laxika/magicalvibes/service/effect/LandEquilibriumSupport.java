package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.LandEquilibriumEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.beans.factory.ObjectProvider;
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
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;
    private final ThreadLocal<Boolean> putterIsNotEnteringController = ThreadLocal.withInitial(() -> false);

    public LandEquilibriumSupport(GameQueryService gameQueryService,
                                  PlayerInputService playerInputService,
                                  @Lazy DestructionSupport destructionSupport,
                                  ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider) {
        this.gameQueryService = gameQueryService;
        this.playerInputService = playerInputService;
        this.destructionSupport = destructionSupport;
        this.permanentChoiceHandlerServiceProvider = permanentChoiceHandlerServiceProvider;
    }

    /**
     * Runs {@code entry} (a battlefield placement) for a land put onto the battlefield by a player other than its
     * controller. The replacement then applies to the player putting the land, not to the entering controller.
     */
    public void runWithPutterOtherThanController(Runnable entry) {
        boolean previous = putterIsNotEnteringController.get();
        putterIsNotEnteringController.set(true);
        try {
            entry.run();
        } finally {
            putterIsNotEnteringController.set(previous);
        }
    }

    public ReplacementPlan findPlan(GameData gameData, UUID enteringControllerId, Permanent entering) {
        if (putterIsNotEnteringController.get() || !gameQueryService.isLand(gameData, entering)) {
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
                replacementCount += (int) permanent.getCard().getStaticEffects().stream()
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

    /**
     * Applies the next of {@code remainingReplacements} Land Equilibrium replacements: {@code sacrificingPlayerId}
     * sacrifices a land, chosen through a permanent choice when there are several. A lone land is sacrificed at
     * once, but a land creature with competing death replacements is asked about before it leaves (CR 616.1):
     * the sacrifice stops, and its controller's answer completes it as if the land had been chosen, which also
     * finishes the rest of the entry (the next replacement, the land play's ETB and "plays a land" triggers).
     *
     * @return {@code true} when a choice is pending and its completion finishes the entry
     */
    public boolean beginNext(GameData gameData, UUID sacrificingPlayerId, Card enteringCard,
                             Zone landPlayZone, int remainingReplacements) {
        if (remainingReplacements <= 0) {
            return false;
        }

        List<Permanent> lands = controlledLands(gameData, sacrificingPlayerId).stream()
                .filter(land -> !gameQueryService.cantBeChosenToSacrifice(gameData, land))
                .toList();
        if (lands.isEmpty()) {
            return false;
        }
        if (lands.size() == 1) {
            Permanent land = lands.getFirst();
            PendingInteraction.PermanentChoice asChosen = new PendingInteraction.PermanentChoice(
                    sacrificingPlayerId, List.of(land.getId()), List.of(),
                    new PermanentChoiceContext.LandEquilibriumSacrifice(
                            sacrificingPlayerId, enteringCard, landPlayZone, remainingReplacements), "");
            if (permanentChoiceHandlerServiceProvider.getObject().runAskingDeathReplacementsFirst(gameData,
                    () -> destructionSupport.sacrificeAndLog(gameData, land, sacrificingPlayerId),
                    new GameData.PermanentChoiceReplay(asChosen, land.getId()))) {
                return true;
            }
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
