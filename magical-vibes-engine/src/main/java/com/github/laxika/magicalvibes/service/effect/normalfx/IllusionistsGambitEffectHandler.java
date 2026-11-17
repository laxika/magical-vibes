package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.IllusionistsGambitEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IllusionistsGambitEffectHandler implements NormalEffectHandlerBean {

    private final CombatRemovalSupport combatRemovalSupport;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IllusionistsGambitEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> attackers = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttacking()) {
                attackers.add(permanent);
            }
        });

        for (Permanent attacker : attackers) {
            combatRemovalSupport.removeFromCombat(gameData, entry, attacker);
            tapUntapSupport.untapPermanent(gameData, attacker);
        }

        gameData.additionalCombatPhasesOnly++;
        int combatPhase = AdditionalCombatBeginningSupport.register(gameData, entry, List.of(), 1, 0);
        // "Each of those creatures attacks that combat if able. They can't attack you or planeswalkers you control
        // that combat": rules changes of this resolution that apply during the added combat phase.
        UUID protectedPlayerId = entry.getControllerId();
        for (Permanent attacker : attackers) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(),
                    new MatchingCreaturesMustAttackEffect(new PermanentIsCreaturePredicate()), attacker.getId(),
                    Duration.duringCombatPhase(combatPhase)));
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), entry.getCard().getName(),
                    entry.getSourcePermanentId(), protectedPlayerId,
                    new CreaturesCantAttackControllerUnlessPredicateEffect(
                            new PermanentNotPredicate(new PermanentTruePredicate()), true),
                    attacker.getId(), protectedPlayerId, null, Duration.duringCombatPhase(combatPhase), 0L));
        }
    }
}
