package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAnyNumberOfTimesPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayManaAnyNumberOfTimesPutCountersOnSelfEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayManaAnyNumberOfTimesPutCountersOnSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayManaAnyNumberOfTimesPutCountersOnSelfEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenPayments = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenPayments == 0) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " chooses not to pay for " + cardName + "'s ability."));
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            int affordablePayments = affordablePayments(pool, e.manaCost(), chosenPayments);
            if (affordablePayments < chosenPayments) {
                beginPaymentChoice(gameData, entry, e);
                return;
            }

            ManaCost payment = new ManaCost(e.manaCost());
            for (int i = 0; i < chosenPayments; i++) {
                payment.payResolutionCost(pool);
            }
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays " + e.manaCost() + " " + chosenPayments
                            + " time(s) for " + cardName + "'s ability."));

            if (e.thenEffect() != null) {
                CardEffect reflexive = SequenceEffect.of(
                        new PutCountersOnSelfEffect(e.counterType(), new XValue()), e.thenEffect());
                queueReflexiveTargetedAbility(gameData, entry, reflexive, chosenPayments);
            } else {
                // "When you pay this cost one or more times, put that many counters on it" is a
                // reflexive triggered ability (CR 603.12): it goes on the stack separately, with the number of
                // payments as its X.
                StackEntry reflexive = ReflexiveAbilitySupport.record(gameData, entry,
                        List.of(new PutCountersOnSelfEffect(e.counterType(), new XValue())));
                reflexive.setXValue(chosenPayments);
                triggeredAbilityStackService.putMade(gameData, reflexive);
            }
            return;
        }

        beginPaymentChoice(gameData, entry, e);
    }

    private void beginPaymentChoice(GameData gameData, StackEntry entry,
                                    PayManaAnyNumberOfTimesPutCountersOnSelfEffect effect) {
        UUID controllerId = entry.getControllerId();
        int maxPayments = maxPotentialPayments(gameData, controllerId, effect.manaCost());
        if (maxPayments <= 0) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxPayments,
                        "Pay " + effect.manaCost() + " any number of times for "
                                + entry.getCard().getName() + "?",
                        entry.getCard().getName(),
                        true,
                        effect.manaCost()));
    }

    private void queueReflexiveTargetedAbility(GameData gameData, StackEntry entry,
                                               CardEffect thenEffect, int paymentCount) {
        // The ability records what the resolving entry was about whichever way it reaches the stack, with
        // the chosen number as its X.
        StackEntry record = ReflexiveAbilitySupport.record(gameData, entry, List.of(thenEffect));
        record.setXValue(paymentCount);
        triggeredAbilityStackService.putMade(gameData, record);
    }

    private int maxPotentialPayments(GameData gameData, UUID playerId, String manaCost) {
        VirtualManaPool virtualPool = potentialManaService.buildVirtualManaPool(gameData, playerId);
        return affordablePayments(virtualPool, manaCost, Integer.MAX_VALUE);
    }

    private int affordablePayments(ManaPool pool, String manaCost, int maximum) {
        if (pool == null) {
            return 0;
        }

        ManaPool remaining = pool instanceof VirtualManaPool virtual
                ? new VirtualManaPool(virtual) : new ManaPool(pool);
        ManaCost payment = new ManaCost(manaCost);
        int affordable = 0;
        while (affordable < maximum && payment.canPayResolutionCost(remaining)) {
            payment.payResolutionCost(remaining);
            affordable++;
        }
        return affordable;
    }
}
