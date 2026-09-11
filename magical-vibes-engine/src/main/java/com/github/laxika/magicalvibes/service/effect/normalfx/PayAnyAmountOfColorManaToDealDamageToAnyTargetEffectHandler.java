package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayAnyAmountOfColorManaToDealDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect payEffect =
                (PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect) effect;
        var controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.getOrDefault(controllerId, "Player");
        ManaCost xCost = new ManaCost("{X}");
        ManaPool pool = gameData.playerManaPools.get(controllerId);

        if (gameData.chosenXValue != null) {
            int amount = gameData.chosenXValue;
            gameData.chosenXValue = null;
            if (amount < 0 || !xCost.canPay(pool, amount, payEffect.color(), 0)) {
                beginChoice(gameData, entry, xCost, payEffect.color());
                return;
            }

            if (amount == 0) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " pays no " + payEffect.color().getCode() + " mana for " + cardName + "."));
                return;
            }

            xCost.pay(pool, amount, payEffect.color(), 0);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + amount + "}" + payEffect.color().getCode()
                            + " for " + cardName + "."));
            entry.setEventValue(amount);
            queueReflexiveAbilityEffectHandler.resolve(gameData, entry,
                    new QueueReflexiveAbilityEffect(new DealDamageToAnyTargetEffect(new EventValue()), false, true));
            return;
        }

        ManaColor color = payEffect.color();
        int maxAmount = xCost.calculateMaxX(
                potentialManaService.buildVirtualManaPool(gameData, controllerId), color, 0);
        if (maxAmount <= 0) {
            return;
        }
        beginChoice(gameData, entry, xCost, color);
    }

    private void beginChoice(GameData gameData, StackEntry entry, ManaCost xCost, ManaColor color) {
        int maxAmount = xCost.calculateMaxX(
                potentialManaService.buildVirtualManaPool(gameData, entry.getControllerId()), color, 0);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), maxAmount,
                "You may pay any amount of {" + color.getCode() + "} for "
                        + entry.getCard().getName() + ". It deals that much damage to any target.",
                entry.getCard().getName(), true));
    }
}
