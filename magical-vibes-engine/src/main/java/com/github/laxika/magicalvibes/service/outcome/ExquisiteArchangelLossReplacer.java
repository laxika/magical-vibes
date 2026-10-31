package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndStartingLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles Exquisite Archangel's one-shot game-loss replacement.
 */
@Component
@Slf4j
public class ExquisiteArchangelLossReplacer implements LossReplacer {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    public ExquisiteArchangelLossReplacer(GameQueryService gameQueryService,
                                          GameLogService gameLogService,
                                          LifeSupport lifeSupport,
                                          @Lazy PermanentRemovalService permanentRemovalService,
                                          @Lazy PlayerInputService playerInputService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.lifeSupport = lifeSupport;
        this.permanentRemovalService = permanentRemovalService;
        this.playerInputService = playerInputService;
    }

    @Override
    public boolean tryReplace(GameData gameData, UUID losingPlayerId, LossReason reason) {
        if (losingPlayerId == null) {
            return false;
        }

        List<Permanent> angels = findReplacingArchangels(gameData, losingPlayerId);
        if (angels.isEmpty()) {
            return false;
        }
        if (LossReplacer.askBeforeLifeTotal(lifeSupport, gameData, losingPlayerId, reason, gameData.startingLife())) {
            return true;
        }
        if (angels.size() == 1) {
            return applyReplacement(gameData, losingPlayerId, angels.getFirst(), reason);
        }

        // CR 616.1: with several replacements available the affected player picks which one applies.
        if (gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null
                && gameData.interaction.permanentChoiceContext()
                instanceof PermanentChoiceContext.ExquisiteArchangelLossReplacement pending
                && pending.playerId().equals(losingPlayerId)) {
            return true;
        }
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ExquisiteArchangelLossReplacement(losingPlayerId, reason.name()));
        playerInputService.beginPermanentChoice(gameData, losingPlayerId,
                angels.stream().map(Permanent::getId).toList(),
                "Choose which Exquisite Archangel replaces your loss of the game.");
        return true;
    }

    /** Exiles {@code angel} instead of letting the player lose, then resets their life total. */
    public boolean applyReplacement(GameData gameData, UUID losingPlayerId, Permanent angel, LossReason reason) {
        if (LossReplacer.askBeforeLifeTotal(lifeSupport, gameData, losingPlayerId, reason, gameData.startingLife())) {
            return true;
        }
        String playerName = gameData.playerIdToName.get(losingPlayerId);
        String angelName = angel.getCard().getName();
        if (!permanentRemovalService.removePermanentToExile(gameData, angel)) {
            return false;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.text(
                playerName + " would lose the game — " + angelName + " is exiled instead."));
        lifeSupport.applySetLifeTotal(gameData, losingPlayerId, gameData.startingLife());
        gameLogService.append(gameData, GameLog.text(
                playerName + "'s life total becomes " + gameData.getLife(losingPlayerId) + "."));
        log.info("Game {} - {} loss ({}) replaced by {}", gameData.id, playerName, reason, angelName);
        return true;
    }

    /**
     * Every Archangel the player controls that still has its loss-replacement ability: a
     * face-down creature has no abilities, and one that lost all abilities no longer has the
     * static ability.
     */
    private List<Permanent> findReplacingArchangels(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        List<Permanent> result = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            boolean hasReplacement = permanent.getCard().getStaticEffects().stream()
                    .anyMatch(ReplaceControllerLossWithExileAndStartingLifeEffect.class::isInstance);
            if (hasReplacement) {
                result.add(permanent);
            }
        }
        return result;
    }
}
