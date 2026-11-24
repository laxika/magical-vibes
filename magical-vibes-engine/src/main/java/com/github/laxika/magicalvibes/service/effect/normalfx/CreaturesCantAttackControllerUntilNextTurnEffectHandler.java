package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link CreaturesCantAttackControllerUntilNextTurnEffect}: the attacking and protected players are found as it
 * resolves, and each protected player gets a floating {@link CreaturesCantAttackControllerUnlessPredicateEffect}
 * (no creature exempt) naming each attacking player, or every player, for the effect's duration.
 */
@Component
@RequiredArgsConstructor
public class CreaturesCantAttackControllerUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreaturesCantAttackControllerUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var ban = (CreaturesCantAttackControllerUntilNextTurnEffect) effect;
        List<UUID> attackers = ban.attackers() == null ? Arrays.asList((UUID) null)
                : subjectResolutionService.players(gameData, entry, ban, ban.attackers());
        for (UUID protectedPlayerId : subjectResolutionService.players(gameData, entry, ban, ban.protectedPlayers())) {
            for (UUID attackingPlayerId : attackers) {
                if (protectedPlayerId.equals(attackingPlayerId)) {
                    continue;
                }
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                        entry.getControllerId(),
                        new CreaturesCantAttackControllerUnlessPredicateEffect(
                                new PermanentNotPredicate(new PermanentTruePredicate()), ban.protectsPlaneswalkers(),
                                attackingPlayerId),
                        null, protectedPlayerId, null, ban.duration(), 0L));
            }
        }
    }
}
