package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndCreateTreasureTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfControllerPoisonedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterlashEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves counter-spell effects on the stack.
 *
 * <p>Handles unconditional counters ({@link CounterSpellEffect}) and conditional
 * "counter unless pays" counters ({@link CounterUnlessPaysEffect}), including
 * uncounterable checks and the copy cessation rule (CR 707.10a).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CounterResolutionService {

    private final GraveyardService graveyardService;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final StateTriggerService stateTriggerService;
    private final PermanentControlResolutionService permanentControlResolutionService;

    /**
     * Resolves an unconditional counter spell (e.g. Cancel, Counterspell).
     *
     * <p>Finds the targeted spell on the stack and counters it. Does nothing if the
     * target is no longer on the stack or is uncounterable.
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter spell being resolved
     */
    @HandlesEffect(CounterSpellEffect.class)
    void resolveCounterSpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        counterSpell(gameData, entry, targetEntry);
    }

    /**
     * Resolves a counter spell that exiles the countered spell instead of putting it
     * into its owner's graveyard (e.g. Dissipate).
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter spell being resolved
     */
    @HandlesEffect(CounterSpellAndExileEffect.class)
    void resolveCounterSpellAndExile(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        counterSpellAndExile(gameData, entry, targetEntry);
    }

    /**
     * Resolves a conditional counter that only counters if the target spell's controller
     * is poisoned (has at least one poison counter). Used by Corrupted Resolve.
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter spell being resolved
     */
    @HandlesEffect(CounterSpellIfControllerPoisonedEffect.class)
    void resolveCounterSpellIfControllerPoisoned(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID targetControllerId = targetEntry.getControllerId();
        int poisonCounters = gameData.playerPoisonCounters.getOrDefault(targetControllerId, 0);
        if (poisonCounters > 0) {
            counterSpell(gameData, entry, targetEntry);
        } else {
            log.info("Game {} - {} controller is not poisoned, spell not countered",
                    gameData.id, targetEntry.getCard().getName());
        }
    }

    /**
     * Resolves a conditional "counter unless pays" effect (e.g. Mana Leak, Syncopate).
     *
     * <p>If the targeted spell's controller cannot pay the required mana, the spell is countered
     * immediately. Otherwise, a {@link PendingMayAbility} is queued to ask the controller whether
     * they want to pay. Supports X-value spells ({@code useXValue}) and exile-on-counter
     * ({@code exileIfCountered}).
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter ability/spell being resolved
     * @param effect   the effect carrying the mana amount (or X-value flag) and exile flag
     */
    @HandlesEffect(CounterUnlessPaysEffect.class)
    void resolveCounterUnlessPays(GameData gameData, StackEntry entry, CounterUnlessPaysEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        int payAmount = effect.useXValue() ? entry.getXValue() : effect.amount();
        UUID targetControllerId = targetEntry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(targetControllerId);
        ManaCost cost = new ManaCost("{" + payAmount + "}");

        if (!cost.canPay(pool)) {
            if (effect.exileIfCountered()) {
                counterSpellAndExile(gameData, entry, targetEntry);
            } else {
                counterSpell(gameData, entry, targetEntry);
            }
        } else {
            // Can pay — ask the opponent via the may ability system
            // Carry the resolved amount (concrete value, useXValue=false) and preserve exileIfCountered
            String prompt = "Pay {" + payAmount + "} to prevent " + targetEntry.getCard().getName() + " from being countered?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), targetControllerId,
                    List.of(new CounterUnlessPaysEffect(payAmount, false, effect.exileIfCountered())),
                    prompt, targetCardId
            ));
        }
    }

    /**
     * Resolves a counter spell that also creates Treasure tokens equal to the countered spell's
     * mana value (e.g. Spell Swindle).
     *
     * <p>If the target spell cannot be countered (uncounterable or protected), the counter part
     * does nothing but Treasure tokens are still created based on the spell's mana value — the
     * spell resolves as much as possible per MTG rules.</p>
     *
     * @param gameData the current game state
     * @param entry    the stack entry of the counter spell being resolved
     */
    @HandlesEffect(CounterSpellAndCreateTreasureTokensEffect.class)
    void resolveCounterSpellAndCreateTreasureTokens(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        // Find the target on the stack — don't use findCounterTarget since that returns null
        // for uncounterable spells, but we still need the mana value for Treasure creation
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return;
        }

        // Read mana value before countering — includes X value for X spells (CR 202.3e)
        int manaValue = targetEntry.getCard().getManaValue() + targetEntry.getXValue();

        // Try to counter the spell
        boolean countered = false;
        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
        } else if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), entry)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    entry.getCard().getColor().name().toLowerCase());
        } else {
            counterSpell(gameData, entry, targetEntry);
            countered = true;
        }

        if (!countered) {
            log.info("Game {} - {} could not counter {}, but still creating Treasure tokens",
                    gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());
        }

        // Create Treasure tokens equal to the spell's mana value regardless of counter success
        if (manaValue > 0) {
            CreateTokenEffect treasures = CreateTokenEffect.ofTreasureToken(manaValue);
            permanentControlResolutionService.applyCreateToken(
                    gameData, entry.getControllerId(), treasures, entry.getCard().getSetCode());
        }
    }

    /**
     * Resolves Counterlash: counters the targeted spell, then lets the controller may-cast a spell
     * from hand that shares a card type with the countered spell, without paying its mana cost.
     *
     * <p>Per Counterlash rulings: if the target spell can't be countered, the controller may still
     * cast a spell from hand. If the target is no longer on the stack (illegal target), the whole
     * spell fizzles and no casting is offered.</p>
     *
     * @param gameData the current game state
     * @param entry    the stack entry of Counterlash being resolved
     */
    @HandlesEffect(CounterlashEffect.class)
    void resolveCounterlash(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        // Find target on stack — need it even if uncounterable (for card types)
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counterlash target no longer on stack", gameData.id);
            return;
        }

        // Gather card types from the targeted spell
        Set<CardType> matchingTypes = new HashSet<>();
        matchingTypes.add(targetEntry.getCard().getType());
        matchingTypes.addAll(targetEntry.getCard().getAdditionalTypes());

        // Try to counter the spell (still offer casting even if uncounterable)
        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
        } else if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), entry)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    entry.getCard().getColor().name().toLowerCase());
        } else {
            counterSpell(gameData, entry, targetEntry);
        }

        // "You may cast a spell that shares a card type with it from your hand without paying its mana cost"
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) return;

        List<Card> eligible = hand.stream()
                .filter(c -> !c.hasType(CardType.LAND) && sharesCardType(c, matchingTypes))
                .toList();

        if (!eligible.isEmpty()) {
            // Queue one may ability per eligible card (reversed so first card is first prompt)
            for (int i = eligible.size() - 1; i >= 0; i--) {
                Card c = eligible.get(i);
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        c, controllerId,
                        List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                        "Cast " + c.getName() + " without paying its mana cost?"
                ));
            }
        }
    }

    private boolean sharesCardType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) return true;
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) return true;
        }
        return false;
    }

    /**
     * Locates the targeted spell on the stack and validates that it can be countered.
     *
     * @param gameData     the current game state
     * @param targetCardId the card ID of the spell being targeted
     * @param counterSource the stack entry of the spell/ability attempting to counter
     * @return the target {@link StackEntry}, or {@code null} if the target is no longer on the
     *         stack or is uncounterable
     */
    private StackEntry findCounterTarget(GameData gameData, UUID targetCardId, StackEntry counterSource) {
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return null;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            return null;
        }

        if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), counterSource)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    counterSource.getCard().getColor().name().toLowerCase());
            return null;
        }

        return targetEntry;
    }

    /**
     * Counters the target spell: removes it from the stack, moves it to the graveyard
     * (unless it is a copy, per CR 707.10a), and broadcasts a log message.
     *
     * @param gameData the current game state
     * @param source   the stack entry of the spell/ability doing the countering
     * @param target   the stack entry of the spell being countered
     */
    private void counterSpell(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        // CR 603.8 — clean up state-trigger tracking when countered
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        // Abilities just cease to exist when countered — only spells go to graveyard
        boolean isAbility = target.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || target.getEntryType() == StackEntryType.TRIGGERED_ABILITY;

        // Copies cease to exist per rule 707.10a — skip graveyard
        if (!target.isCopy() && !isAbility) {
            graveyardService.addCardToGraveyard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = isAbility
                ? target.getCard().getName() + "'s ability is countered."
                : target.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }

    /**
     * Counters the target spell and exiles it instead of putting it into its owner's graveyard.
     * Copies cease to exist per CR 707.10a.
     *
     * @param gameData the current game state
     * @param source   the stack entry of the spell/ability doing the countering
     * @param target   the stack entry of the spell being countered
     */
    private void counterSpellAndExile(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        // CR 603.8 — clean up state-trigger tracking when countered
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        // Copies cease to exist per rule 707.10a — skip exile
        if (!target.isCopy()) {
            exileService.exileCard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = target.getCard().getName() + " is countered and exiled.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered and exiled {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }
}


