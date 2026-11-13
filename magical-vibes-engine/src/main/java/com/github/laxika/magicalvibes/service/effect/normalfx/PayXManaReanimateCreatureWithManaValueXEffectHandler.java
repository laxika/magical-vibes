package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreatureWithManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link PayXManaReanimateCreatureWithManaValueXEffect}: prompt for X, pay it, then put the
 * reflexive "return target creature card with mana value X from your graveyard to the battlefield"
 * trigger on the stack with its target already chosen (Isareth the Awakener).
 *
 * <p>The reflexive trigger is a normal pre-targeted {@link ReturnCardFromGraveyardEffect}, so the
 * corpse counter and the exile-instead-of-leaving replacement ride along on the existing graveyard
 * return path.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaReanimateCreatureWithManaValueXEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    /** "Creature card with mana value X": X is the X paid, which the reflexive ability's entry has as its X. */
    private static final CardPredicate CREATURE_WITH_MANA_VALUE_X = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardManaValueEqualsXPredicate()));

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaReanimateCreatureWithManaValueXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaReanimateCreatureWithManaValueXEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String cardName = sourceCard.getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Re-entry after the player chose X
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " pays {0} for " + cardName + "."));
                log.info("Game {} - {} pays 0 mana for {}", gameData.id, playerName, cardName);
                beginReflexiveTrigger(gameData, entry, e, controllerId, 0);
                return;
            }

            // Cap was based on potential mana so the player could tap lands during the
            // prompt; re-check the actual pool before charging.
            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                log.info("Game {} - {} cannot yet pay X={} for {} — re-prompting",
                        gameData.id, playerName, chosenValue, cardName);
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }
            new ManaCost("{X}").pay(pool, chosenValue);

            gameLogService.append(gameData, GameLog.text(playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            log.info("Game {} - {} pays {} mana for {}", gameData.id, playerName, chosenValue, cardName);
            beginReflexiveTrigger(gameData, entry, e, controllerId, chosenValue);
            return;
        }

        // First call: cap includes untapped mana sources so an empty pool with untapped
        // lands still opens the prompt (CR 605.3a — mana abilities during the payment). Paying
        // {0} is always possible, so the prompt opens even with no mana at all.
        beginXPrompt(gameData, controllerId, cardName);
    }

    /**
     * Puts the reflexive trigger on the stack with a creature card of mana value {@code x} from the
     * controller's graveyard as its target. A single legal card is targeted without a prompt; several
     * let the controller pick one; none means the trigger simply does not go on the stack.
     */
    private void beginReflexiveTrigger(GameData gameData, StackEntry entry,
                                       PayXManaReanimateCreatureWithManaValueXEffect e,
                                       UUID controllerId, int x) {
        Card sourceCard = entry.getCard();
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(CREATURE_WITH_MANA_VALUE_X)
                .targetGraveyard(true)
                .enterWithCounter(e.enterWithCounter())
                .enterWithCounterCount(e.enterWithCounter() == null ? 0 : 1)
                .exileIfLeavesBattlefield(e.exileIfLeavesBattlefield())
                .build();
        // The ability records what the resolving entry was about, with the X paid as its X.
        StackEntry record = ReflexiveAbilitySupport.record(
                gameData, entry, List.of(returnEffect), sourceCard.getName() + "'s ability");
        record.setXValue(x);
        // "When you do" is an ability of its own (CR 603.12): its graveyard target is chosen as it goes on the stack,
        // against the X paid.
        triggeredAbilityStackService.putMade(gameData, record);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        String prompt = "Pay {X} for " + cardName + "? Return a creature card with mana value X from your graveyard.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName, true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources;
    }

    /**
     * Mana in the pool that may pay this cost right now. Artifact-only and Myr-only mana is
     * restricted to casting spells and activating abilities, so it can't pay a triggered ability's cost.
     */
    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getXCostOnlyColorless();
    }
}
