package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedAdditionalCombatBeginningEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IllusionistsGambitAdditionalCombatEffect;
import com.github.laxika.magicalvibes.model.effect.IllusionistsGambitEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

        UUID attackingPlayerId = gameData.activePlayerId;
        if (attackingPlayerId == null) {
            attackingPlayerId = entry.getControllerId();
        }
        List<UUID> attackerIds = attackers.stream().map(Permanent::getId).toList();
        gameData.additionalCombatPhasesOnly++;
        gameData.queueDelayedAction(new DelayedAdditionalCombatBeginningEffect(
                attackingPlayerId,
                entry.getCard(),
                new IllusionistsGambitAdditionalCombatEffect(
                        Set.copyOf(attackerIds), entry.getControllerId()),
                entry.getSourcePermanentId()));
    }
}
