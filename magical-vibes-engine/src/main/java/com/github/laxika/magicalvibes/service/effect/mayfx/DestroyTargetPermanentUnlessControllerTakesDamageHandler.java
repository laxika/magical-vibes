package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetPermanentUnlessControllerTakesDamageEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes a targeted permanent's damage-or-destruction choice. */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentUnlessControllerTakesDamageHandler implements MayEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DestroyTargetPermanentUnlessControllerTakesDamageEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentUnlessControllerTakesDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(DestroyTargetPermanentUnlessControllerTakesDamageEffect.class::isInstance)
                .map(DestroyTargetPermanentUnlessControllerTakesDamageEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
        if (accepted) {
            DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                    effect.damage(), DamageRecipient.TARGET_PLAYER);
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(damage)),
                    ability.controllerId(), ability.sourcePermanentId());
            damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
        } else {
            effectHandler.destroyTargetPermanent(gameData, ability.sourceCard(), ability.targetCardId());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
