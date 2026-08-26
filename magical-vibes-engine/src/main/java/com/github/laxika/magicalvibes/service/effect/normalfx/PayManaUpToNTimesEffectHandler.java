package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaUpToNTimesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayManaUpToNTimesEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayManaUpToNTimesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayManaUpToNTimesEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenPayments = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenPayments < 0 || chosenPayments > e.maximumPayments()) {
                beginPaymentChoice(gameData, entry, e);
                return;
            }
            if (chosenPayments == 0) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " chooses not to pay for " + cardName + "'s ability."));
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (affordablePayments(pool, e.manaCost(), chosenPayments) < chosenPayments) {
                beginPaymentChoice(gameData, entry, e);
                return;
            }

            ManaCost payment = new ManaCost(e.manaCost());
            for (int i = 0; i < chosenPayments; i++) {
                payment.pay(pool);
            }
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays " + e.manaCost() + " " + chosenPayments
                            + " time(s) for " + cardName + "'s ability."));

            if (e.thenEffect() != null) {
                queueReflexiveTargetedAbility(gameData, entry,
                        limitModeCount(e.thenEffect(), chosenPayments), chosenPayments);
            }
            return;
        }

        beginPaymentChoice(gameData, entry, e);
    }

    private void beginPaymentChoice(GameData gameData, StackEntry entry,
                                    PayManaUpToNTimesEffect effect) {
        UUID controllerId = entry.getControllerId();
        int maxPayments = maxPotentialPayments(gameData, controllerId, effect);
        if (maxPayments <= 0) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxPayments,
                        "Pay " + effect.manaCost() + " up to " + effect.maximumPayments()
                                + " times for " + entry.getCard().getName() + "?",
                        entry.getCard().getName(),
                        true,
                        effect.manaCost()));
    }

    private void queueReflexiveTargetedAbility(GameData gameData, StackEntry entry,
                                               CardEffect thenEffect, int paymentCount) {
        if (thenEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(thenEffect), null, 0, 0, paymentCount));
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            return;
        }
        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                entry.getCard(), entry.getControllerId(), List.of(thenEffect), entry.getSourcePermanentId(),
                List.of(), 0, 0, List.of(), paymentCount, List.of(), false));
        etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
    }

    private CardEffect limitModeCount(CardEffect effect, int paymentCount) {
        if (effect instanceof ChooseOneEffect choices) {
            return new ChooseOneEffect(choices.options(), choices.optional(), choices.choicesRequired(),
                    Math.min(paymentCount, choices.choicesMax()), choices.allModesWhenOptionalCostPaid(),
                    choices.additionalModesCondition());
        }
        return effect;
    }

    private int maxPotentialPayments(GameData gameData, UUID playerId,
                                     PayManaUpToNTimesEffect effect) {
        VirtualManaPool virtualPool = potentialManaService.buildVirtualManaPool(gameData, playerId);
        return affordablePayments(virtualPool, effect.manaCost(), effect.maximumPayments());
    }

    private int affordablePayments(ManaPool pool, String manaCost, int maximum) {
        if (pool == null) {
            return 0;
        }

        ManaPool remaining = pool instanceof VirtualManaPool virtual
                ? new VirtualManaPool(virtual) : new ManaPool(pool);
        ManaCost payment = new ManaCost(manaCost);
        int affordable = 0;
        while (affordable < maximum && payment.canPay(remaining)) {
            payment.pay(remaining);
            affordable++;
        }
        return affordable;
    }
}
