package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificePermanentsEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice for Fandaniel's sacrifice-or-life-loss clause. */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySacrificeNontokenCreatureOrLoseLifeHandler
        implements MayEffectHandlerBean {

    private final EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect) ability.effects().getFirst();
        if (accepted && effectHandler.hasLegalSacrifice(gameData, ability)) {
            UUID sourceControllerId = effectHandler.sourceControllerId(gameData, ability);
            var sacrifice = new SacrificePermanentsEffect(
                    1,
                    EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffectHandler.NONTOKEN_CREATURE,
                    SacrificeRecipient.TARGET_PLAYER);
            var sacrificeEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    sourceControllerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(sacrifice)),
                    ability.controllerId(),
                    ability.sourcePermanentId());
            sacrificeEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            sacrificePermanentsEffectHandler.resolve(gameData, sacrificeEntry, sacrifice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        effectHandler.applyLifeLoss(gameData, ability, effect.lifeLoss());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
