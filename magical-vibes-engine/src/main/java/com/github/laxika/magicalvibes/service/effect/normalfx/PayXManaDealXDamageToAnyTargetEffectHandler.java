package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PayXManaDealXDamageToAnyTargetEffect}: prompts the controller for X (capped by
 * how much of {@code manaCost} they can pay, including mana from untapped sources), charges
 * {@code manaCost}, then deals X damage to the target chosen when the ability was put on the
 * stack (or, for the reflexive form, queues a reflexive damage ability whose target is chosen after payment). Choosing X=0 declines. Mana abilities may be activated while the prompt is open
 * (frontend allows tapping lands during {@code XValueChoice}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaDealXDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaDealXDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaDealXDamageToAnyTargetEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null && !e.reflexive()) return;

        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        ManaCost cost = new ManaCost(e.manaCost());
        ManaPool pool = gameData.playerManaPools.get(controllerId);

        // Re-entry after the player chose X value
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " declines to pay for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            // Cap was based on potential mana so the player could tap lands during the prompt;
            // re-check the actual pool before charging.
            if (!cost.canPay(pool, chosenValue)) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay " + e.manaCost().replace("{X}", "{" + chosenValue + "}")
                                + " for " + cardName + " (tap mana sources, then choose X again)."));
                log.info("Game {} - {} cannot yet pay X={} for {} — re-prompting",
                        gameData.id, playerName, chosenValue, cardName);
                beginXPrompt(gameData, controllerId, cost, e.manaCost(), cardName);
                return;
            }

            cost.pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(playerName + " pays " + e.manaCost().replace("{X}", "{" + chosenValue + "}") + " for " + cardName + "."));
            log.info("Game {} - {} pays X={} for {}", gameData.id, playerName, chosenValue, cardName);

            if (e.reflexive()) {
                queueReflexiveDamage(gameData, entry, e, chosenValue);
                return;
            }

            int rawDamage = chosenValue;
            damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        // First call: prompt for X. Cap includes untapped mana sources so an empty pool with
        // untapped lands still opens the Pay / Don't Pay UI (CR 605.3 — mana abilities before cost).
        // maxX is 0 both when X=0 is affordable and when the base cost is not, so test the base cost itself.
        if (!cost.canPay(potentialManaService.buildVirtualManaPool(gameData, controllerId))) {
            gameLogService.append(gameData, GameLog.text(playerName + " can't pay for " + cardName + "'s ability."));
            log.info("Game {} - {} can't pay {} for {}", gameData.id, playerName, e.manaCost(), cardName);
            return;
        }
        beginXPrompt(gameData, controllerId, cost, e.manaCost(), cardName);
    }

    private void queueReflexiveDamage(GameData gameData, StackEntry entry,
                                      PayXManaDealXDamageToAnyTargetEffect e, int damage) {
        CardEffect reflexiveDamage = e.targetRestriction().admits(TargetPredicate.Kind.PERMANENT)
                ? new DealDamageToAnyTargetEffect(damage)
                : new DealDamageToPlayersEffect(damage, DamageRecipient.TARGET_PLAYER);
        queueReflexiveAbilityEffectHandler.resolve(gameData, entry, new QueueReflexiveAbilityEffect(reflexiveDamage));
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, ManaCost cost, String manaCost, String cardName) {
        int maxX = cost.calculateMaxX(potentialManaService.buildVirtualManaPool(gameData, controllerId));
        String prompt = "You may pay " + manaCost + " for " + cardName
                + ". Choose X (0 = don't pay). It deals X damage to the target.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName, true, manaCost));
    }
}
