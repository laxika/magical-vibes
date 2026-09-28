package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Entrapment Maneuver's target-player attacking-creature sacrifice. */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, entry.getControllerId())) {
            return;
        }

        List<UUID> attackingCreatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.isAttacking()
                        && gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                    attackingCreatureIds.add(permanent.getId());
                }
            }
        }

        if (attackingCreatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no attacking creature to sacrifice."));
            return;
        }

        if (attackingCreatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, attackingCreatureIds.getFirst());
            if (creature != null) {
                sacrificeAndCreateTokens(gameData, creature, targetPlayerId, entry.getControllerId(),
                        entry.getCard(), e.tokenTemplate());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughness(
                        targetPlayerId, entry.getControllerId(), entry.getCard(), e.tokenTemplate()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, attackingCreatureIds,
                entry.getCard().getName() + " — Choose an attacking creature to sacrifice.");
    }

    public void sacrificeAndCreateTokens(GameData gameData, Permanent creature, UUID sacrificingPlayerId,
                                          UUID tokenCreatingPlayerId, Card sourceCard,
                                          CreateTokenEffect tokenTemplate) {
        int toughness = Math.max(0, gameQueryService.getEffectiveToughness(gameData, creature));
        destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);

        if (toughness > 0) {
            permanentControlSupport.applyCreateToken(
                    gameData, tokenCreatingPlayerId, tokenTemplate, toughness, sourceCard.getSetCode());
        }

        log.info("Game {} - {} creates {} tokens after {} sacrifices {}",
                gameData.id, gameData.playerIdToName.get(tokenCreatingPlayerId), toughness,
                sourceCard.getName(), creature.getCard().getName());
    }
}
