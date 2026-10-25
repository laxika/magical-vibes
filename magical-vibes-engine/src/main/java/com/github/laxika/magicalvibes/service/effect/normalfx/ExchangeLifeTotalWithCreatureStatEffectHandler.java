package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeLifeTotalWithCreatureStatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Exchanges a player's life total with the source creature's power or toughness (Tree of Perdition, Evra).
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the life total is its first change, so a Lich's Duel Mastery the
 * player's life loss would sacrifice instead (CR 701.12g) is asked about (CR 616.1) before the life total or the
 * creature changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeLifeTotalWithCreatureStatEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeLifeTotalWithCreatureStatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExchangeLifeTotalWithCreatureStatEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        boolean usePower = e.stat() == ExchangeLifeTotalWithCreatureStatEffect.Stat.POWER;
        String statName = usePower ? "power" : "toughness";

        UUID playerId = e.recipient() == ExchangeLifeTotalWithCreatureStatEffect.Recipient.TARGET_PLAYER
                ? entry.getTargetId()
                : entry.getControllerId();
        if (playerId == null) {
            gameLogService.append(gameData, GameLog.text("No player to exchange with. Exchange doesn't occur."));
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameLogService.append(gameData, GameLog.text("Source creature is no longer on the battlefield. Exchange doesn't occur."));
            return;
        }

        // CR 208.3: a noncreature permanent has no power or toughness, so there is no stat to exchange.
        if (!gameQueryService.isCreature(gameData, source)) {
            gameLogService.append(gameData, GameLog.text("Source is no longer a creature. Exchange doesn't occur."));
            return;
        }

        // CR 701.12g: the player's new life total is the creature's current effective stat
        int currentStat = usePower
                ? gameQueryService.getEffectivePower(gameData, source)
                : gameQueryService.getEffectiveToughness(gameData, source);
        int currentLife = gameData.getLife(playerId);
        currentStat = gameData.capLifeTotal(playerId, currentStat);

        String playerName = gameData.playerIdToName.get(playerId);
        // CR 701.12a: the life change goes through the shared life-total path, which applies the
        // can't-change, can't-gain and can't-lose prohibitions and the life gain/loss replacements.
        // When it is blocked the exchange does not occur at all.
        if (!lifeSupport.applySetLifeTotal(gameData, playerId, currentStat)) {
            return;
        }

        if (currentLife == currentStat) {
            gameLogService.append(gameData, GameLog.builder().text(playerName + " exchanges life total with ").card(source.getCard()).text("'s " + statName + " (both at " + currentLife + ").").build());
        } else {
            gameLogService.append(gameData, GameLog.builder().text(playerName + " exchanges life total with ").card(source.getCard()).text("'s " + statName + " (" + playerName + ": " + currentLife + " -> " + currentStat + ", ").card(source.getCard()).text(" " + statName + ": " + currentStat + " -> " + currentLife + ").").build());
        }

        // Set creature's base stat to the player's former life total (layer 7b setting effect,
        // CR 613.4b). The timestamp orders the override against other 7b setters in the layered pass.
        if (usePower) {
            source.setBasePowerOverriddenPermanently(true);
            source.setPermanentBasePowerOverride(currentLife);
            source.setPermanentBasePowerOverrideTimestamp(gameData.nextTimestamp());
        } else {
            source.setBaseToughnessOverriddenPermanently(true);
            source.setPermanentBaseToughnessOverride(currentLife);
            source.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());
        }

        log.info("Game {} - {} exchanges life ({} -> {}) with {}'s {} ({} -> {})",
                gameData.id, playerName, currentLife, currentStat,
                source.getCard().getName(), statName, currentStat, currentLife);
    }
}
