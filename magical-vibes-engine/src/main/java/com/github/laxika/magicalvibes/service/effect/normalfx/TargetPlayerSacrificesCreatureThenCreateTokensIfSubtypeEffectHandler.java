package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect}: the targeted
 * player sacrifices a creature of their choice, and if that creature had the required subtype the
 * same player creates the template tokens (Warren Weirding).
 *
 * <p>When the target player controls exactly one creature it is sacrificed automatically; with
 * several, they are prompted via {@code SacrificeCreatureCreateTokensIfSubtype} and the follow-up
 * runs in {@code PermanentChoiceBattlefieldHandlerService}. The shared sacrifice-and-token logic
 * lives in {@link #sacrificeAndMaybeCreateTokens}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentControlSupport permanentControlSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            log.info("Game {} - {} fizzles (no valid target player)", gameData.id, entry.getCard().getName());
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        if (creatureIds.isEmpty()) {
            String logEntry = playerName + " has no creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures to sacrifice for {}", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                sacrificeAndMaybeCreateTokens(gameData, targetPlayerId, creature, e.requiredSubtype(),
                        e.tokenTemplate(), entry.getCard().getSetCode());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeCreatureCreateTokensIfSubtype(
                        targetPlayerId, entry.getCard(), e.requiredSubtype(), e.tokenTemplate()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                entry.getCard().getName() + " — Choose a creature to sacrifice.");
    }

    /**
     * Sacrifices {@code creature} for {@code sacrificingPlayerId}, then — if that creature had
     * {@code requiredSubtype} (last-known info, evaluated before removal) — has the same player
     * create the template tokens. Shared by the auto-sacrifice path and the choice handler.
     */
    public void sacrificeAndMaybeCreateTokens(GameData gameData, UUID sacrificingPlayerId, Permanent creature,
                                              CardSubtype requiredSubtype, CreateTokenEffect tokenTemplate,
                                              String sourceSetCode) {
        boolean hadSubtype = predicateEvaluationService.matchesPermanentPredicate(
                gameData, creature, new PermanentHasSubtypePredicate(requiredSubtype));

        permanentRemovalService.removePermanentToGraveyard(gameData, creature);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());

        if (hadSubtype) {
            permanentControlSupport.applyCreateToken(gameData, sacrificingPlayerId, tokenTemplate, sourceSetCode);
        }
    }
}
