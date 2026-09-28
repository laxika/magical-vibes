package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Entrapment Maneuver's targeted attacking-creature sacrifice. */
@Component
@RequiredArgsConstructor
public class TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
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

        List<UUID> validIds = eligibleAttackingCreatureIds(gameData, targetPlayerId);
        if (validIds.isEmpty()) {
            return;
        }

        if (validIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, validIds.getFirst());
            sacrificeAndCreateTokens(gameData, creature, targetPlayerId, entry, e.tokenTemplate());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughness(
                        targetPlayerId, entry, e.tokenTemplate()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, validIds,
                entry.getCard().getName() + " â€” Choose an attacking creature to sacrifice.");
    }

    public void sacrificeAndCreateTokens(GameData gameData, Permanent creature, UUID sacrificingPlayerId,
                                          StackEntry entry, CreateTokenEffect tokenTemplate) {
        if (creature == null
                || !sacrificingPlayerId.equals(gameQueryService.findPermanentController(gameData, creature.getId()))
                || !gameQueryService.isCreature(gameData, creature)
                || !creature.isAttacking()
                || gameQueryService.cantBeSacrificed(gameData, creature)
                || !gameQueryService.canEffectCauseSacrifice(gameData, sacrificingPlayerId, entry.getControllerId())) {
            return;
        }

        int toughness = Math.max(0, gameQueryService.getEffectiveToughness(gameData, creature));
        destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);

        if (toughness > 0) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), tokenTemplate, toughness, entry.getCard().getSetCode()));
        }
    }

    private List<UUID> eligibleAttackingCreatureIds(GameData gameData, UUID playerId) {
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return validIds;
        }
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && permanent.isAttacking()
                    && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                validIds.add(permanent.getId());
            }
        }
        return validIds;
    }
}
