package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureOrCreatesTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Wasitora's damaged-player sacrifice-or-Cat-Dragon trigger. */
@Component
@RequiredArgsConstructor
public class TargetPlayerSacrificesCreatureOrCreatesTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesCreatureOrCreatesTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrificeOrToken = (TargetPlayerSacrificesCreatureOrCreatesTokenEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<UUID> creatureIds = eligibleCreatureIds(gameData, targetPlayerId, entry.getControllerId());
        if (creatureIds.isEmpty()) {
            createTokenEffectHandler.resolve(gameData, entry, sacrificeOrToken.tokenTemplate());
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                destructionSupport.sacrificeAndLog(gameData, creature, targetPlayerId);
            } else {
                createTokenEffectHandler.resolve(gameData, entry, sacrificeOrToken.tokenTemplate());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerSacrificesCreatureOrCreatesToken(
                        targetPlayerId, entry, sacrificeOrToken.tokenTemplate()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                entry.getCard().getName() + " — Choose a creature to sacrifice.");
    }

    public void sacrificeOrCreateToken(GameData gameData, Permanent creature,
                                       PermanentChoiceContext.TargetPlayerSacrificesCreatureOrCreatesToken context) {
        UUID sacrificingPlayerId = context.sacrificingPlayerId();
        boolean canSacrifice = gameQueryService.findPermanentById(gameData, creature.getId()) != null
                && sacrificingPlayerId.equals(gameQueryService.findPermanentController(gameData, creature.getId()))
                && gameQueryService.isCreature(gameData, creature)
                && !gameQueryService.cantBeSacrificed(gameData, creature)
                && gameQueryService.canEffectCauseSacrifice(gameData, sacrificingPlayerId,
                context.resolvingEntry().getControllerId());
        if (canSacrifice) {
            destructionSupport.sacrificeAndLog(gameData, creature, sacrificingPlayerId);
        } else {
            createTokenEffectHandler.resolve(gameData, context.resolvingEntry(), context.tokenTemplate());
        }
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (!gameData.playerIds.contains(playerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return List.of();
        }
        return destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
    }
}
