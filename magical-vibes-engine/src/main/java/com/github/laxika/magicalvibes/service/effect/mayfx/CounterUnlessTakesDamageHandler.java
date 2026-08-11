package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.CounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Completes the damage-or-counter choice for {@link CounterUnlessTakesDamageEffect}.
 */
@Component
@RequiredArgsConstructor
public class CounterUnlessTakesDamageHandler implements MayEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessTakesDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(CounterUnlessTakesDamageEffect.class::isInstance)
                .map(CounterUnlessTakesDamageEffect.class::cast)
                .findFirst()
                .orElseThrow();
        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
        StackEntry sourceEntry = new StackEntry(
                StackEntryType.INSTANT_SPELL, ability.sourceCard(), sourceControllerId,
                ability.sourceCard().getName(), List.of(effect), ability.targetCardId(), (UUID) null);
        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, ability.targetCardId(), sourceEntry);
        if (targetEntry == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                    effect.damage(), DamageRecipient.TARGET_SPELL_CONTROLLER);
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
                    ability.sourceCard().getName() + "'s effect", new ArrayList<>(List.of(damage)),
                    ability.targetCardId(), (UUID) null);
            dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
        } else {
            counterSupport.counterSpell(gameData, sourceEntry, targetEntry);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
