package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentCreatureAndPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a non-targeting choice of an opponent-controlled creature for a perpetual modification. */
@Component
@RequiredArgsConstructor
public class ChooseOpponentCreatureAndPerpetuallyBoostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PerpetuallyBoostTargetCreatureEffectHandler perpetuallyBoostTargetCreatureEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentCreatureAndPerpetuallyBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (ChooseOpponentCreatureAndPerpetuallyBoostEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> creatureIds = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureIds.add(permanent.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            return;
        }

        var context = new PermanentChoiceContext.ChooseOpponentCreatureAndPerpetuallyBoost(
                entry.getSourcePermanentId(), entry.getCard(), controllerId,
                boost.powerBoost(), boost.toughnessBoost());
        if (creatureIds.size() == 1) {
            completeChoice(gameData, creatureIds.getFirst(), context);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " — Choose a creature an opponent controls.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.ChooseOpponentCreatureAndPerpetuallyBoost context) {
        StackEntry boostEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                context.sourceCard(),
                context.controllerId(),
                context.sourceCard().getName() + "'s ability",
                List.of(),
                chosenPermanentId,
                context.sourcePermanentId());
        perpetuallyBoostTargetCreatureEffectHandler.resolve(
                gameData, boostEntry,
                new PerpetuallyBoostTargetCreatureEffect(context.powerBoost(), context.toughnessBoost()));
    }
}
