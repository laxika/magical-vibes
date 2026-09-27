package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetCreatureOrPlaneswalkerEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Completes a targeted permanent's damage-or-controller-damage choice. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageHandler
        implements MayEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DealDamageToTargetCreatureOrPlaneswalkerEffectHandler
            dealDamageToTargetCreatureOrPlaneswalkerEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect.class::isInstance)
                .map(DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
        if (accepted) {
            DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                    effect.controllerDamage(), DamageRecipient.TARGET_PLAYER);
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(damage)),
                    ability.controllerId(), ability.sourcePermanentId());
            damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);

            if (effect.controllerDamageFollowUp() != null) {
                gameData.queueMayAbilityForPlayer(ability.sourceCard(), sourceControllerId,
                        effect.controllerDamageFollowUp(), null, ability.sourcePermanentId(),
                        sourceControllerId, ability.sourcePermanentSnapshot());
            }
        } else {
            DealDamageToTargetCreatureOrPlaneswalkerEffect damage =
                    new DealDamageToTargetCreatureOrPlaneswalkerEffect(effect.targetDamage());
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(damage)),
                    ability.targetCardId(), ability.sourcePermanentId());
            damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            dealDamageToTargetCreatureOrPlaneswalkerEffectHandler.resolve(gameData, damageEntry, damage);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
