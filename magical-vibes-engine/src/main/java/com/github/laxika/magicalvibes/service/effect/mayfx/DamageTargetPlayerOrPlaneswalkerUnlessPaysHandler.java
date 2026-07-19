package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetPlayerOrPlaneswalkerEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Quenchable Fire's delayed pay-or-take-damage prompt. The affected party may pay the stored mana
 * cost; declining (or being unable to pay) deals the damage to the originally-targeted player or
 * planeswalker — carried on {@code ability.targetCardId} — through the normal damage system.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageTargetPlayerOrPlaneswalkerUnlessPaysHandler implements MayEffectHandlerBean {

    private final DealDamageToTargetPlayerOrPlaneswalkerEffectHandler damageHandler;
    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect effect = ability.effects().stream()
                .filter(e -> e instanceof DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect)
                .map(e -> (DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect) e)
                .findFirst().orElseThrow();

        UUID payerId = ability.controllerId();
        UUID targetId = ability.targetCardId();

        if (accepted) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(payerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + " pays "
                        + ability.manaCost() + ". (" + ability.sourceCard().getName() + ")"));
                log.info("Game {} - {} pays {} to avoid {}'s delayed damage", gameData.id,
                        player.getUsername(), ability.manaCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but can't actually pay — fall through to the damage.
        }

        DealDamageToTargetPlayerOrPlaneswalkerEffect damage =
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(effect.damage());
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), payerId,
                ability.sourceCard().getName() + "'s delayed ability", new ArrayList<>(List.of(damage)),
                targetId, (UUID) null);
        damageHandler.resolve(gameData, damageEntry, damage);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
