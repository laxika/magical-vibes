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
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Handles one opponent's choice for Fandaniel's sacrifice-or-life-loss clause. An accepted sacrifice
 * changes nothing before it, and neither does the life loss of an opponent who doesn't sacrifice, so a
 * creature with competing death replacements, or a Lich's Duel Mastery that loss would sacrifice, is
 * asked about before it leaves (CR 616.1) and the answer is completed again with that choice.
 */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySacrificeNontokenCreatureOrLoseLifeHandler
        implements MayEffectHandlerBean {

    private final EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect) ability.effects().getFirst();
        if (accepted && effectHandler.hasLegalSacrifice(gameData, ability)) {
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true, () -> sacrifice(gameData, ability));
            return;
        }

        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, accepted, () -> {
                    effectHandler.applyLifeLoss(gameData, ability, effect.lifeLoss());
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                });
    }

    private void sacrifice(GameData gameData, PendingMayAbility ability) {
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
    }
}
