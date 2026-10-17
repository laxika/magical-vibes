package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's payment and token-copy choice for Reflect. */
@Component
@RequiredArgsConstructor
public class ReflectMayEffectHandler implements MayEffectHandlerBean {

    private final com.github.laxika.magicalvibes.service.effect.normalfx.ReflectEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReflectEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var reflect = (ReflectEffect) ability.effects().getFirst();
        if (accepted && pay(gameData, player, reflect.manaCost())) {
            CreateTokenCopyOfSourceEffect copyEffect =
                    CreateTokenCopyOfSourceEffect.withoutSourceEffect(ReflectEffect.class);
            StackEntry tokenEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    player.getId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(copyEffect)),
                    null,
                    reflect.sourcePermanentId());
            tokenEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            // "When they do, they create a token copy" is a reflexive triggered ability (CR 603.12).
            gameData.stack.add(tokenEntry);
        }

        effectHandler.completeChoice(gameData, ability, reflect);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private boolean pay(GameData gameData, Player player, String manaCost) {
        ManaCost cost = new ManaCost(manaCost);
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (!cost.canPayResolutionCost(pool)) {
            return false;
        }
        cost.payResolutionCost(pool);
        return true;
    }
}
