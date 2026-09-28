package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AttackingPlayerChoosesCreatureToBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttackingPlayerChoosesCreatureToBoostEffectHandler implements NormalEffectHandlerBean {

    private final BoostTargetCreatureEffectHandler boostTargetCreatureEffectHandler;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttackingPlayerChoosesCreatureToBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (AttackingPlayerChoosesCreatureToBoostEffect) effect;
        UUID attackingPlayerId = entry.getTargetId();
        if (attackingPlayerId == null) {
            return;
        }

        List<UUID> attackingCreatureIds = attackingCreatureIds(gameData, attackingPlayerId);
        if (attackingCreatureIds.isEmpty()) {
            return;
        }

        var context = new PermanentChoiceContext.AttackingPlayerChoosesCreatureToBoost(
                entry.getCard(), entry.getSourcePermanentId(), entry.getControllerId(),
                attackingPlayerId, choiceEffect.powerBoost(), choiceEffect.toughnessBoost());
        if (attackingCreatureIds.size() == 1) {
            completeChoice(gameData, attackingCreatureIds.getFirst(), context);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, attackingPlayerId, attackingCreatureIds,
                entry.getCard().getName() + " — choose an attacking creature to boost.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.AttackingPlayerChoosesCreatureToBoost context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null
                || !context.attackingPlayerId().equals(gameQueryService.findPermanentController(gameData, chosen.getId()))
                || !chosen.isAttacking()
                || !gameQueryService.isCreature(gameData, chosen)) {
            return;
        }

        BoostTargetCreatureEffect boost = new BoostTargetCreatureEffect(
                context.powerBoost(), context.toughnessBoost());
        StackEntry boostEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                context.sourceCard(),
                context.controllerId(),
                context.sourceCard().getName() + "'s ability",
                List.of(boost),
                chosen.getId(),
                context.sourcePermanentId());
        boostTargetCreatureEffectHandler.resolve(gameData, boostEntry, boost);
    }

    private List<UUID> attackingCreatureIds(GameData gameData, UUID attackingPlayerId) {
        List<UUID> ids = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(attackingPlayerId, List.of())) {
            if (permanent.isAttacking() && gameQueryService.isCreature(gameData, permanent)) {
                ids.add(permanent.getId());
            }
        }
        return ids;
    }
}
