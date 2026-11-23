package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * The Brothers' War chapter II: "Until your next turn, each creature they control attacks the other chosen player
 * each combat if able." The requirement doesn't change characteristics or control, so it modifies the rules of the
 * game and binds every creature each chosen player controls while it lasts, one they gain or that enters later too
 * (CR 611.2c): one floating {@link MatchingCreaturesMustAttackEffect} per chosen player, read by the attack rules.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect attackEffect =
                (TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect) effect;
        List<UUID> targetPlayers = entry.targetsForGroup(attackEffect.targetGroup());
        if (targetPlayers.size() < 2) {
            targetPlayers = entry.getTargetIds();
        }
        if (targetPlayers.size() < 2) {
            return;
        }

        UUID firstPlayer = targetPlayers.get(0);
        UUID secondPlayer = targetPlayers.get(1);
        if (firstPlayer.equals(secondPlayer)
                || !gameData.playerIds.contains(firstPlayer)
                || !gameData.playerIds.contains(secondPlayer)) {
            return;
        }

        addAttackRequirement(gameData, entry, firstPlayer, secondPlayer);
        addAttackRequirement(gameData, entry, secondPlayer, firstPlayer);
    }

    private static void addAttackRequirement(GameData gameData, StackEntry entry,
                                             UUID creatureControllerId, UUID requiredAttackTargetId) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new MatchingCreaturesMustAttackEffect(
                        new PermanentControlledByPlayerPredicate(creatureControllerId), requiredAttackTargetId),
                creatureControllerId, Duration.UNTIL_YOUR_NEXT_TURN));
    }
}
