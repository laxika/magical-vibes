package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndLifeTotalEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * "If you would lose the game, instead exile this permanent and your life total becomes N" (The Golden Throne's 1,
 * Exquisite Archangel's starting life total): one source per permanent with
 * {@link ReplaceControllerLossWithExileAndLifeTotalEffect} its controller controls, face up and with its abilities.
 * Every loss reason is replaced (Golden Throne rulings); the player chooses among several sources with the other loss
 * replacements (CR 616.1).
 */
@Component
@Slf4j
public class ExileSourceAndSetLifeTotalLossReplacer implements LossReplacer {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final AmountEvaluationService amountEvaluationService;

    public ExileSourceAndSetLifeTotalLossReplacer(GameQueryService gameQueryService,
                                                  GameLogService gameLogService,
                                                  LifeSupport lifeSupport,
                                                  @Lazy PermanentRemovalService permanentRemovalService,
                                                  @Lazy AmountEvaluationService amountEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.lifeSupport = lifeSupport;
        this.permanentRemovalService = permanentRemovalService;
        this.amountEvaluationService = amountEvaluationService;
    }

    @Override
    public List<Permanent> sources(GameData gameData, UUID losingPlayerId, LossReason reason) {
        List<Permanent> sources = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(losingPlayerId, List.of())) {
            if (replacement(gameData, permanent) != null) {
                sources.add(permanent);
            }
        }
        return sources;
    }

    @Override
    public boolean replace(GameData gameData, UUID losingPlayerId, Permanent source, LossReason reason) {
        ReplaceControllerLossWithExileAndLifeTotalEffect replacement = replacement(gameData, source);
        if (replacement == null) {
            return false;
        }
        int lifeTotal = amountEvaluationService.evaluate(gameData, replacement.lifeTotal(),
                AmountContext.forStaticEffect(source, losingPlayerId));
        if (LossReplacer.askBeforeLifeTotal(lifeSupport, gameData, losingPlayerId, reason, lifeTotal, source)) {
            return true;
        }
        String playerName = gameData.playerIdToName.get(losingPlayerId);
        String sourceName = FaceDownVisibility.publicName(gameData, source);
        if (!permanentRemovalService.removePermanentToExile(gameData, source)) {
            return false;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.text(
                playerName + " would lose the game — " + sourceName + " is exiled instead."));
        lifeSupport.applySetLifeTotal(gameData, losingPlayerId, lifeTotal);
        gameLogService.append(gameData, GameLog.text(
                playerName + "'s life total becomes " + gameData.getLife(losingPlayerId) + "."));
        log.info("Game {} - {} loss ({}) replaced by {}", gameData.id, playerName, reason, sourceName);
        return true;
    }

    /**
     * The permanent's loss replacement, or {@code null}: a face-down permanent has none of its card's abilities and one
     * that lost its printed abilities no longer has the static ability (CR 708.2a, 613.1f).
     */
    private ReplaceControllerLossWithExileAndLifeTotalEffect replacement(GameData gameData, Permanent permanent) {
        if (permanent.isFaceDown()) {
            return null;
        }
        ReplaceControllerLossWithExileAndLifeTotalEffect found = null;
        for (CardEffect effect : permanent.getCard().getStaticEffects()) {
            if (effect instanceof ReplaceControllerLossWithExileAndLifeTotalEffect replacement) {
                found = replacement;
                break;
            }
        }
        if (found == null || gameQueryService.hasLostPrintedAbilities(gameData, permanent)) {
            return null;
        }
        return found;
    }
}
