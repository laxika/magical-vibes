package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Set;

/**
 * Resolves the "At end of combat" trigger carrying {@link PutCounterOnCombatOpponentAtEndOfCombatEffect}:
 * puts the configured counters on each creature still blocking or blocked by the source. Greater
 * Werewolf-style "put a -0/-2 counter on each creature blocking or blocked by this creature."
 */
@Component
@RequiredArgsConstructor
public class PutCounterOnCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnCombatOpponentAtEndOfCombatEffect counterEffect =
                (PutCounterOnCombatOpponentAtEndOfCombatEffect) effect;

        if (counterEffect.amount() <= 0) {
            return;
        }
        Set<UUID> combatOpponentIds = gameData.combatBlockOpponentIdsThisCombat
                .getOrDefault(entry.getSourcePermanentId(), Set.of());
        for (UUID combatOpponentId : combatOpponentIds) {
            Permanent combatOpponent = gameQueryService.findPermanentById(gameData, combatOpponentId);
            // A creature removed from combat since the block (e.g. by regeneration) is no longer
            // blocking or blocked, so it does not receive the counter.
            if (combatOpponent != null && gameQueryService.isCreature(gameData, combatOpponent)
                    && (combatOpponent.isBlocking() || combatOpponent.isAttacking())) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, combatOpponent,
                        counterEffect.counterType(), counterEffect.amount());
                if (counterEffect.alsoTap()) {
                    combatOpponent.tap();
                }
                if (counterEffect.counterType() == CounterType.PARALYZATION) {
                    CombatService.grantParalyzationRemoveAbility(combatOpponent);
                    if (combatOpponent.getCounterCount(CounterType.PARALYZATION) > 0) {
                        gameData.addFloatingEffect(new FloatingContinuousEffect(
                                UUID.randomUUID(), entry.getCard().getName(), null,
                                entry.getControllerId(), DoesntUntapEffect.self(),
                                combatOpponent.getId(), null,
                                new PermanentHasCountersPredicate(CounterType.PARALYZATION,
                                        combatOpponent.getLastCounterRemovalVersions()
                                                .getOrDefault(CounterType.PARALYZATION, 0L)),
                                EffectDuration.PERMANENT, 0));
                    }
                }
            }
        }
    }
}
