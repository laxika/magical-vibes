package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes a targeted creature's damage-or-controller-damage choice. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureUnlessControllerTakesDamageHandler
        implements MayEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DealDamageToTargetCreatureEffectHandler dealDamageToTargetCreatureEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureUnlessControllerTakesDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(DealDamageToTargetCreatureUnlessControllerTakesDamageEffect.class::isInstance)
                .map(DealDamageToTargetCreatureUnlessControllerTakesDamageEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
        if (accepted) {
            DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                    effect.controllerDamage(), DamageRecipient.TARGET_PLAYER);
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", List.of(damage),
                    ability.controllerId(), ability.sourcePermanentId());
            damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
        } else {
            DealDamageToTargetCreatureEffect damage = new DealDamageToTargetCreatureEffect(
                    effect.targetDamage());
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", List.of(damage),
                    ability.targetCardId(), ability.sourcePermanentId());
            damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            dealDamageToTargetCreatureEffectHandler.resolve(gameData, damageEntry, damage);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
