package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Wasitora's combat-damage sacrifice-or-token ability. */
@Component
@RequiredArgsConstructor
public class TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffect sacrificeOrToken =
                (TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<UUID> creatureIds = eligibleCreatureIds(gameData, targetPlayerId, entry.getControllerId());
        if (creatureIds.isEmpty()) {
            createTokenEffectHandler.resolve(gameData, entry, sacrificeOrToken.token());
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                destructionSupport.sacrificeAndLog(gameData, creature, targetPlayerId);
            } else {
                createTokenEffectHandler.resolve(gameData, entry, sacrificeOrToken.token());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.EachOpponentCreatesTokenUnlessSacrificesCreature(
                        targetPlayerId,
                        entry.getControllerId(),
                        entry.getCard(),
                        entry.getSourcePermanentId(),
                        entry.getSourcePermanentSnapshot(),
                        sacrificeOrToken.token(),
                        List.of()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                entry.getCard().getName() + " - Choose a creature to sacrifice.");
    }

    /**
     * Completes the target player's creature choice: the chosen creature is sacrificed while it can still be, else
     * the token is created.
     */
    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EachOpponentCreatesTokenUnlessSacrificesCreature context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        boolean canSacrifice = creature != null
                && context.sacrificingPlayerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                && gameQueryService.isCreature(gameData, creature)
                && !gameQueryService.cantBeSacrificed(gameData, creature)
                && gameQueryService.canEffectCauseSacrifice(gameData, context.sacrificingPlayerId(),
                context.sourceControllerId());
        if (canSacrifice) {
            destructionSupport.sacrificeAndLog(gameData, creature, context.sacrificingPlayerId());
            return;
        }
        StackEntry sourceEntry = StackEntry.carrier(context.sourceCard(), context.sourceControllerId(),
                context.sourceCard().getName() + "'s ability", new ArrayList<>(), 0, context.sourcePermanentId());
        sourceEntry.setSourcePermanentSnapshot(context.sourcePermanentSnapshot());
        createTokenEffectHandler.resolve(gameData, sourceEntry, context.token());
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return List.of();
        }
        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeChosenToSacrifice(gameData, permanent)) {
                    creatureIds.add(permanent.getId());
                }
            }
        }
        return creatureIds;
    }
}
