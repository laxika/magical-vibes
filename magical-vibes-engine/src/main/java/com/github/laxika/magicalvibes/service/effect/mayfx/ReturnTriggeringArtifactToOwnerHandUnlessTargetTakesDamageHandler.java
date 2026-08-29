package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes the targeted player's damage-or-return choice for Pia's Revolution. */
@Component
@RequiredArgsConstructor
public class ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageHandler
        implements MayEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect.class::isInstance)
                .map(ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect.class::cast)
                .findFirst()
                .orElseThrow();
        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();

        if (accepted) {
            DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                    effect.damage(), DamageRecipient.TARGET_PLAYER);
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(damage)), ability.targetCardId(), ability.sourcePermanentId());
            damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
        } else {
            effectHandler.returnArtifactToOwnerHand(gameData, effect.triggeringArtifactId());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
