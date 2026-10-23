package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.ForcedCostOrElseEffectHandler;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Optional "you may pay [cost]; if you don't, [penalty]" (e.g. Yawgmoth Demon).
 * A decline whose first fallback effect kills (Phantasmal Forces' "sacrifice this creature") changes
 * nothing before that death, so a creature with competing death replacements is asked about before it
 * leaves (CR 616.1) and the same decline is completed again with that choice.
 */
@Component
@RequiredArgsConstructor
public class ForcedCostOrElseHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForcedCostOrElseEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Runnable answer = () -> mayPenaltyChoiceHandlerService.handleForcedCostOrElseOptionalChoice(
                gameData, player, accepted, ability);
        if (!accepted && declineKillsBeforeChangingAnything(ability)) {
            permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                    gameData, player, ability, answer);
        } else {
            answer.run();
        }
    }

    /**
     * Whether declining {@code ability} goes straight to a fallback effect that kills first. A villainous
     * choice counts its remaining choices and an "any player may pay" prompt offers the next player before
     * the fallback, and an enchanted or defending player's prompt without a recorded source controller reads
     * it from {@link GameData#forcedCostOrElseSourceControllerId}, which the decline clears; a replay would
     * repeat or lose those, so they stay outside the window.
     */
    private static boolean declineKillsBeforeChangingAnything(PendingMayAbility ability) {
        ForcedCostOrElseEffect effect = ability.effects().stream()
                .filter(ForcedCostOrElseEffect.class::isInstance)
                .map(ForcedCostOrElseEffect.class::cast)
                .findFirst().orElseThrow();
        boolean sourceControllerFromGameState = ability.sourceControllerId() == null
                && (effect.payerIsEnchantedController() || effect.payerIsDefendingPlayer());
        return !effect.villainousChoice()
                && !effect.anyPlayerMayPay()
                && !sourceControllerFromGameState
                && !effect.elseEffects().isEmpty()
                && ForcedCostOrElseEffectHandler.killsBeforeChangingAnything(effect.elseEffects().getFirst());
    }
}
