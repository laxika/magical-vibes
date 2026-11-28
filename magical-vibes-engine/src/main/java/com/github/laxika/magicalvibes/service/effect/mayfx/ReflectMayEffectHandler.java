package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's payment and token-copy choice for Reflect. */
@Component
@RequiredArgsConstructor
public class ReflectMayEffectHandler implements MayEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ReflectEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final ResolutionCostPaymentService costPayment;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReflectEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var reflect = (ReflectEffect) ability.effects().getFirst();
        // A tax paid to the reflecting permanent's controller (Tax Taker).
        if (accepted && costPayment.payManaFromPool(gameData, player.getId(), new ManaCost(reflect.manaCost()),
                reflect.abilityControllerId())) {
            CreateTokenCopyOfSourceEffect copyEffect =
                    CreateTokenCopyOfSourceEffect.withoutSourceEffect(ReflectEffect.class);
            StackEntry tokenEntry = ReflexiveAbilitySupport.recordFor(gameData,
                    ReflexiveAbilitySupport.resolvingEntryOf(gameData, ability.sourceCard(),
                            reflect.sourcePermanentId()),
                    ability.sourceCard(), player.getId(), List.of(copyEffect),
                    ability.sourceCard().getName() + "'s ability", reflect.sourcePermanentId());
            if (ability.sourcePermanentSnapshot() != null) {
                tokenEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            }
            // "When they do, they create a token copy" is a reflexive triggered ability (CR 603.12).
            triggeredAbilityStackService.putMade(gameData, tokenEntry);
        }

        effectHandler.completeChoice(gameData, ability, reflect);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
