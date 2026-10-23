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
 * A decline, or an accepted mana cost the pool can't pay, whose first fallback effect kills (Phantasmal
 * Forces' "sacrifice this creature", or Icy Prison's after the last player of "unless any player pays" declines)
 * changes nothing before that death, and neither does an accepted sacrifice cost that needs no choice (Pillar
 * Tombs of Aku or Midnight Crusader Shuttle's villainous choice with a lone creature, Hecatomb with exactly
 * four), so a creature with competing death
 * replacements is asked about before it leaves (CR 616.1) and the same answer is completed again with that
 * choice.
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
        ForcedCostOrElseEffect effect = ability.effects().stream()
                .filter(ForcedCostOrElseEffect.class::isInstance)
                .map(ForcedCostOrElseEffect.class::cast)
                .findFirst().orElseThrow();
        boolean unpaidAnswerKills = fallbackKillsBeforeChangingAnything(gameData, effect)
                && (!accepted || mayPenaltyChoiceHandlerService.acceptedCostFallsThroughUnpaid(
                        gameData, ability, effect));
        boolean acceptedAnswerSacrifices = accepted && !countsDownVillainousChoices(gameData, effect)
                && mayPenaltyChoiceHandlerService.acceptedCostSacrificesWithoutChoice(gameData, ability, effect);
        if (unpaidAnswerKills || acceptedAnswerSacrifices) {
            PendingMayAbility replayable = withRecordedSourceController(gameData, ability, effect);
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, replayable, accepted,
                    () -> mayPenaltyChoiceHandlerService.handleForcedCostOrElseOptionalChoice(
                            gameData, player, accepted, replayable));
        } else {
            mayPenaltyChoiceHandlerService.handleForcedCostOrElseOptionalChoice(gameData, player, accepted, ability);
        }
    }

    /**
     * Whether an unpaid answer goes straight to a fallback effect that kills first. A villainous choice
     * counts its remaining choices, and an "any player may pay" prompt that isn't the last one offers the
     * next player, before the fallback; a replay would repeat those, so they stay outside the window.
     */
    private static boolean fallbackKillsBeforeChangingAnything(GameData gameData, ForcedCostOrElseEffect effect) {
        return !effect.villainousChoice()
                && (!effect.anyPlayerMayPay() || gameData.forcedCostOrElseRemainingPlayers.isEmpty())
                && !effect.elseEffects().isEmpty()
                && ForcedCostOrElseEffectHandler.killsBeforeChangingAnything(effect.elseEffects().getFirst());
    }

    /**
     * Whether answering a villainous choice queues it again for an additional choice (The Valeyard) before it
     * pays. The last (or only) choice just marks the villainous choice done, which a replay repeats harmlessly.
     */
    private static boolean countsDownVillainousChoices(GameData gameData, ForcedCostOrElseEffect effect) {
        return effect.villainousChoice() && gameData.villainousChoice.additionalChoicesRemaining > 0;
    }

    /**
     * {@code ability} carrying the source controller an "any player may pay", enchanted-controller or
     * defending-player prompt otherwise reads from {@link GameData#forcedCostOrElseSourceControllerId}. The
     * answer clears that field before its fallback, so the replay after the death replacement choice reads
     * the controller from the ability instead.
     */
    private static PendingMayAbility withRecordedSourceController(GameData gameData, PendingMayAbility ability,
                                                                  ForcedCostOrElseEffect effect) {
        boolean readsSourceControllerFromGameState = ability.sourceControllerId() == null
                && (effect.anyPlayerMayPay() || effect.payerIsEnchantedController() || effect.payerIsDefendingPlayer())
                && gameData.forcedCostOrElseSourceControllerId != null;
        return readsSourceControllerFromGameState
                ? ability.withSourceControllerId(gameData.forcedCostOrElseSourceControllerId)
                : ability;
    }
}
