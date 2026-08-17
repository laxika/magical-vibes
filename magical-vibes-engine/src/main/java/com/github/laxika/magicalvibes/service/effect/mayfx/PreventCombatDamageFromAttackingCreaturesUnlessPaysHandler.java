package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles each attacker controller's pay-or-prevent decision. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreventCombatDamageFromAttackingCreaturesUnlessPaysHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect effect = ability.effects().stream()
                .filter(PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect.class::isInstance)
                .map(PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID payingPlayerId = ability.controllerId();
        boolean paid = false;
        if (accepted) {
            ManaCost cost = new ManaCost(effect.manaCost());
            ManaPool pool = gameData.playerManaPools.get(payingPlayerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                paid = true;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + effect.manaCost() + ". (", ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} for {}", gameData.id, player.getUsername(),
                        effect.manaCost(), ability.sourceCard().getName());
            }
        }

        if (!paid) {
            UUID attackerId = ability.targetCardId();
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            if (attacker != null) {
                gameData.creaturesPreventedFromDealingCombatDamage.add(attackerId);
                gameLogService.append(gameData, GameLog.builder()
                        .text("All combat damage ")
                        .card(attacker.getCard())
                        .text(" would deal this turn. (")
                        .card(ability.sourceCard())
                        .text(")")
                        .build());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
