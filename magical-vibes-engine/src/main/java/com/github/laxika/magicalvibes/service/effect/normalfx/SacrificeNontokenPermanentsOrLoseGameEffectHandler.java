package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeNontokenPermanentsOrLoseGameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Lich's damage-triggered sacrifice-or-lose ability. */
@Component
@RequiredArgsConstructor
public class SacrificeNontokenPermanentsOrLoseGameEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final ControllerLosesGameEffectHandler controllerLosesGameEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeNontokenPermanentsOrLoseGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int requiredCount = entry.getEventValue();
        UUID controllerId = entry.getControllerId();
        if (requiredCount <= 0 || controllerId == null || !gameData.playerIds.contains(controllerId)) {
            return;
        }

        List<Permanent> candidates = nontokenSacrificeCandidates(gameData, controllerId);
        if (candidates.size() < requiredCount) {
            sacrifice(gameData, candidates);
            loseGame(gameData, entry);
        } else if (candidates.size() == requiredCount) {
            sacrifice(gameData, candidates);
        } else {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    controllerId,
                    candidates.stream().map(Permanent::getId).toList(),
                    requiredCount,
                    new MultiPermanentChoiceContext.SacrificeNontokenPermanentsOrLoseGame(requiredCount),
                    "Choose " + requiredCount + " nontoken permanent"
                            + (requiredCount == 1 ? "" : "s") + " to sacrifice.");
        }
    }

    /** Completes the exact-count choice and applies the loss clause when the selection is short. */
    public void completeChoice(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.SacrificeNontokenPermanentsOrLoseGame context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending effect resolution entry");
        }

        UUID controllerId = entry.getControllerId();
        List<Permanent> selected = permanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null
                        && controllerId.equals(gameQueryService.findPermanentController(gameData, permanent.getId()))
                        && !permanent.getCard().isToken()
                        && !gameQueryService.cantBeSacrificed(gameData, permanent))
                .toList();
        sacrifice(gameData, selected);
        if (selected.size() < context.requiredCount()) {
            loseGame(gameData, entry);
        }
    }

    private List<Permanent> nontokenSacrificeCandidates(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> !permanent.getCard().isToken())
                .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                .toList();
    }

    private void sacrifice(GameData gameData, List<Permanent> permanents) {
        destructionSupport.performSimultaneousSacrifice(
                gameData, permanents.stream().map(Permanent::getId).toList());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void loseGame(GameData gameData, StackEntry entry) {
        controllerLosesGameEffectHandler.resolve(gameData, entry, new ControllerLosesGameEffect());
    }
}
