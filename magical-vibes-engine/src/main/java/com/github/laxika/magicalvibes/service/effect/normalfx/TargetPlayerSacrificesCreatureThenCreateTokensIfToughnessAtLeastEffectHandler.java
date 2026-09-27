package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves target-player sacrifice effects whose token count depends on toughness. */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect) effect;
        UUID sacrificingPlayerId = entry.getTargetId();
        if (sacrificingPlayerId == null || !gameData.playerIds.contains(sacrificingPlayerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, sacrificingPlayerId,
                entry.getControllerId())) {
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                    creatureIds.add(permanent.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no creatures to sacrifice."));
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                sacrificeAndCreateTokens(gameData, creature, sacrificingPlayerId,
                        entry.getControllerId(), e, entry.getCard());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeast(
                        sacrificingPlayerId, entry.getControllerId(), entry.getCard(), e.tokenTemplate(),
                        e.toughnessThreshold(), e.normalAmount(), e.increasedAmount()));
        playerInputService.beginPermanentChoice(gameData, sacrificingPlayerId, creatureIds,
                entry.getCard().getName() + " — Choose a creature to sacrifice.");
    }

    public void sacrificeAndCreateTokens(GameData gameData, Permanent creature,
                                          UUID sacrificingPlayerId, UUID tokenControllerId,
                                          TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect effect,
                                          com.github.laxika.magicalvibes.model.Card sourceCard) {
        int toughness = gameQueryService.getEffectiveToughness(gameData, creature);
        destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);

        int amount = toughness >= effect.toughnessThreshold()
                ? effect.increasedAmount() : effect.normalAmount();
        permanentControlSupport.applyCreateToken(gameData, tokenControllerId, effect.tokenTemplate(), amount,
                sourceCard.getSetCode());

        log.info("Game {} - {} creates {} token(s) after {} sacrifices {}",
                gameData.id, gameData.playerIdToName.get(tokenControllerId), amount,
                sourceCard.getName(), creature.getCard().getName());
    }
}
