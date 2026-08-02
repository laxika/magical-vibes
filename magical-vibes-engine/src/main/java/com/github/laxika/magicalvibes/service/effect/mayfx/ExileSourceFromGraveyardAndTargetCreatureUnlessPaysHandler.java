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
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSourceCardFromGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Carrionette's pay-or-be-exiled prompt. The target creature's controller may pay the stored mana
 * cost to stop the whole effect; declining (or being unable to pay) exiles that creature and the
 * source card from its owner's graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSourceFromGraveyardAndTargetCreatureUnlessPaysHandler implements MayEffectHandlerBean {

    private final ExileTargetPermanentEffectHandler exileTargetPermanentEffectHandler;
    private final ExileSourceCardFromGraveyardEffectHandler exileSourceCardFromGraveyardEffectHandler;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID payerId = ability.controllerId();
        UUID targetPermanentId = ability.targetCardId();

        if (accepted) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(payerId);
            if (cost.canPay(pool)) {
                cost.pay(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + ability.manaCost() + ". (",
                        ability.sourceCard(), ")"));
                log.info("Game {} - {} pays {} to stop {}'s exile", gameData.id, player.getUsername(),
                        ability.manaCost(), ability.sourceCard().getName());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but can't actually pay — fall through to the exile.
        }

        ExileTargetPermanentEffect exileTarget = new ExileTargetPermanentEffect();
        StackEntry targetEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), payerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(exileTarget)), targetPermanentId, ability.sourcePermanentId());
        exileTargetPermanentEffectHandler.resolve(gameData, targetEntry, exileTarget);

        ExileSourceCardFromGraveyardEffect exileSource = new ExileSourceCardFromGraveyardEffect();
        StackEntry sourceEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), payerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(exileSource)), (UUID) null, ability.sourcePermanentId());
        exileSourceCardFromGraveyardEffectHandler.resolve(gameData, sourceEntry, exileSource);

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
