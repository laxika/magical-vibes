package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one player's Carrion Rats or Carrion Wurm graveyard-exile choice. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageHandler
        implements MayEffectHandlerBean {

    private final AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffectHandler effectHandler;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();
        List<UUID> remaining = effectHandler.remainingAfter(gameData, effect, chooserId);

        if (accepted) {
            List<CardEffect> effects = new ArrayList<>();
            for (int i = 0; i < effect.cardsToExile(); i++) {
                effects.add(new TargetPlayerExilesCardFromGraveyardEffect(0));
            }
            effects.add(new AssignNoCombatDamageEffect());
            if (!remaining.isEmpty()) {
                effects.add(new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(
                        effect.cardsToExile(), remaining, effect.abilityControllerId(), effect.sourcePermanentId()));
            }
            effectResolutionService.resolveEffects(gameData, new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    effect.abilityControllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    effects,
                    chooserId,
                    effect.sourcePermanentId()));
        } else if (!remaining.isEmpty()) {
            effectHandler.promptNext(gameData, ability.sourceCard(),
                    new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(
                            effect.cardsToExile(), remaining, effect.abilityControllerId(), effect.sourcePermanentId()));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
