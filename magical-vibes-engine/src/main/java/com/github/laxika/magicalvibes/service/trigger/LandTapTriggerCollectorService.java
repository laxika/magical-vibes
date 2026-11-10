package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TriggeredManaAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The trigger collector of the tap-for-mana watches: "whenever a player taps a land for mana" (and the abilities it
 * reads by that trigger: "you tap a land", "an opponent taps a land", "enchanted land is tapped", "you tap this land",
 * {@link TapWatch#ON_ANY_PLAYER_TAPS_LAND}) and "a player taps a permanent for mana", "you tap a creature / a nonland
 * permanent / an artifact token for mana". The caller judged the trigger, its filters, its relation and its options;
 * every ability gets one entry that records the tap whatever its effects are ({@link #tapEntry}), and is
 * <ul>
 *   <li>a triggered mana ability (no target, could add mana, CR 605.1b), resolved at once through its effects'
 *       handlers (CR 605.4a), or parked until the tapped permanent's own mana choice settles the type of mana it
 *       produced when an effect reads it;</li>
 *   <li>a targeted ability, whose target is chosen as it goes on the stack (CR 603.3d) by a choice carrying the same
 *       record, after that mana choice when the produced mana isn't known yet;</li>
 *   <li>any other ability, put on the stack as written (it can't add mana, so it isn't a mana ability and uses the
 *       stack, CR 605.1b).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LandTapTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final ObjectProvider<TriggerCollectionService> triggerCollectionServiceProvider;

    /**
     * "Whenever a player taps a land for mana, …" and the abilities read by that trigger: the entry records the player
     * who tapped the land as its triggering player and non-targeting "that player", the land as its triggering
     * permanent with its last-known information ("that land", CR 608.2h), the mana the tap produced and the source as it
     * was (a permanent, or a face-up plane). "Whenever a player taps a land for mana, this deals 1 damage to that
     * player" (Manabarbs), "… an Island …" (Scald), "… if it's not that player's turn, destroy that land" (Price of
     * Glory), "that player adds one mana of any type that land produced" (Mana Flare), "its controller adds an
     * additional {G}" (Wild Growth), "target opponent creates …" (Forbidden Orchard).
     */
    @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_LAND)
    private boolean handleLandTapAbility(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.LandTap landTap = (TriggerContext.LandTap) ctx;
        return collect(match, effect, landTap.tappingPlayerId(), landTap.tappedLandId(), landTap.producedColors(),
                landTap.tappingPlayerId());
    }

    /**
     * "Whenever a player / an opponent taps a permanent for mana, …" (Eloren Wilds, Treasure Nabber) and "whenever you
     * tap a creature / a nonland permanent / an artifact token for mana, …" (Badgermole Cub, Kinnan, Roxanne), as
     * {@link #handleLandTapAbility}: the entry records the tapping player as its triggering player, the permanent as its
     * triggering permanent and its non-targeting "that permanent" ("gain control of that artifact"), and the mana the
     * tap produced, {@code null} while the permanent's own any-color choice is still open.
     */
    @CollectsTapTriggers({
            @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA),
            @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA),
            @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA),
            @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_CONTROLLER_TAPS_ARTIFACT_TOKEN_FOR_MANA)
    })
    private boolean handlePermanentTapForManaAbility(TriggerMatchContext match, CardEffect effect,
                                                     TriggerContext ctx) {
        UUID tappingPlayerId;
        UUID tappedPermanentId;
        Set<ManaColor> produced;
        switch (ctx) {
            case TriggerContext.PermanentTapForMana tap -> {
                tappingPlayerId = tap.tappingPlayerId();
                tappedPermanentId = tap.tappedPermanentId();
                produced = tap.producedManaTypes();
            }
            case TriggerContext.NonlandPermanentTapForMana tap -> {
                tappingPlayerId = tap.tappingPlayerId();
                tappedPermanentId = tap.tappedPermanentId();
                produced = tap.producedManaTypes();
            }
            case TriggerContext.CreatureTapForMana tap -> {
                tappingPlayerId = tap.tappingPlayerId();
                tappedPermanentId = tap.tappedCreatureId();
                produced = Set.of();
            }
            default -> {
                return false;
            }
        }
        if (produced != null && produced.isEmpty() && readsProducedMana(effect)) {
            return false;
        }
        return collect(match, effect, tappingPlayerId, tappedPermanentId, produced, tappedPermanentId);
    }

    private boolean collect(TriggerMatchContext match, CardEffect effect, UUID tappingPlayerId,
                            UUID tappedPermanentId, Set<ManaColor> producedColors, UUID thatObjectId) {
        GameData gameData = match.gameData();
        boolean producedUnknown = producedColors == null || (producedColors.isEmpty()
                && gameData.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice);
        if (TapAbilityReader.isTriggeredManaEffect(effect)) {
            List<CardEffect> steps = TapAbilityReader.steps(effect);
            StackEntry entry = tapEntry(match, steps, tappingPlayerId, tappedPermanentId, thatObjectId);
            if (producedUnknown && steps.stream().anyMatch(LandTapTriggerCollectorService::readsProducedMana)) {
                park(gameData, entry);
                return true;
            }
            entry.setProducedManaColors(producedColors == null ? Set.of() : producedColors);
            resolveAtOnce(gameData, entry);
            return true;
        }
        TargetSpec targetSpec = effect.targetSpec();
        StackEntry entry = tapEntry(match, List.of(effect), tappingPlayerId, tappedPermanentId, thatObjectId);
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT) || targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            if (producedUnknown) {
                park(gameData, entry);
                return true;
            }
            queueTargetChoice(gameData, entry, producedColors);
            return true;
        }
        entry.setProducedManaColors(producedColors == null ? Set.of() : producedColors);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(entry.getCard()));
        log.info("Game {} - {} triggers on a tap for mana by {}", gameData.id, entry.getCard().getName(),
                gameData.playerIdToName.get(tappingPlayerId));
        return true;
    }

    /**
     * The entry of the ability of {@code match}'s source with {@code effects} for a tap for mana: the tapping player as
     * its triggering player, the tapped permanent as its triggering permanent with its last-known information,
     * {@code thatObjectId} as its non-targeting target ("that player" of a land tap, "that permanent" of a permanent's),
     * and the source as it was when the ability triggered (a permanent, or a face-up plane; a player's granted ability
     * has no source card).
     */
    private StackEntry tapEntry(TriggerMatchContext match, List<CardEffect> effects, UUID tappingPlayerId,
                                UUID tappedPermanentId, UUID thatObjectId) {
        Card sourceCard = match.sourceCard() != null ? match.sourceCard()
                : match.permanent() != null ? match.permanent().getCard() : null;
        Permanent tapped = tappedAsItLastExisted(match.gameData(), tappedPermanentId);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard == null ? "Mana ability" : sourceCard.getName() + "'s ability",
                new ArrayList<>(effects),
                thatObjectId,
                match.permanent() == null ? null : match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPlayerId(tappingPlayerId);
        entry.setTriggeringPermanentId(tappedPermanentId);
        entry.setTriggeringPermanent(tapped);
        entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
        if (match.permanent() != null) {
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        }
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
        return entry;
    }

    /**
     * Parks {@code entry} until the tapped permanent's own mana choice settles the produced mana
     * ({@code ChoiceHandlerService.resolveProducedManaTriggers} resolves a triggered mana ability then, and
     * {@link #queueResolvedProducedManaTriggerTarget} chooses a targeted ability's target).
     */
    private void park(GameData gameData, StackEntry entry) {
        entry.setAwaitingProducedManaColor(true);
        gameData.pendingManaAbilityTriggers.add(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(entry.getCard()));
    }

    /**
     * Resolves the triggered mana ability {@code entry} at once (CR 605.4a), each effect by its handler, reading the
     * tap from the entry.
     */
    private void resolveAtOnce(GameData gameData, StackEntry entry) {
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect effect = entry.getEffectsToResolve().get(i);
            EffectHandler handler = effectHandlerRegistry.getHandler(effect);
            if (handler == null) {
                log.warn("No handler for triggered mana ability effect: {}", effect.getClass().getSimpleName());
                continue;
            }
            entry.setResolvingEffectIndex(i);
            handler.resolve(gameData, entry, effect);
        }
        entry.setResolvingEffectIndex(-1);
    }

    /** Whether {@code effect} is a triggered mana ability's effect that reads the type of mana the tap produced. */
    private static boolean readsProducedMana(CardEffect effect) {
        return effect instanceof TriggeredManaAbilityEffect mana && mana.readsProducedMana();
    }

    /**
     * The tapped permanent as it is on the battlefield, or as it last existed there once its mana ability made it leave
     * (Gemstone Mine's last use, a sacrificed Treasure), for "that land" (CR 608.2h); {@code null} when neither is known.
     */
    private Permanent tappedAsItLastExisted(GameData gameData, UUID tappedPermanentId) {
        Permanent tapped = gameQueryService.findPermanentById(gameData, tappedPermanentId);
        if (tapped != null) {
            return tapped;
        }
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> snapshot.getId().equals(tappedPermanentId))
                .findFirst().orElse(null);
    }

    /**
     * Completes a parked targeted ability (C.A.M.P.: it targets, so it isn't a mana ability, CR 605.1b) once the tapped
     * permanent's mana choice settled {@code manaColor}: its target is chosen as it goes on the stack, with the entry's
     * record of the tap. {@code false} for a parked triggered mana ability, which the caller resolves.
     */
    public boolean queueResolvedProducedManaTriggerTarget(GameData gameData, StackEntry entry,
            ManaColor manaColor) {
        if (entry.getEffectsToResolve().stream().noneMatch(effect -> effect.targetSpec().admits(
                TargetPredicate.Kind.PERMANENT) || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            return false;
        }
        queueTargetChoice(gameData, entry, Set.of(manaColor));
        return true;
    }

    /**
     * Queues the target choice of the targeted ability {@code entry} (CR 603.3d): the targets its effects declare, a
     * player target narrowed by its effect's player predicate; the choice carries the entry's record of the tap and
     * {@code producedManaColors}.
     */
    private void queueTargetChoice(GameData gameData, StackEntry entry, Set<ManaColor> producedManaColors) {
        List<CardEffect> effects = List.copyOf(entry.getEffectsToResolve());
        boolean playerTargetOnly = effects.stream().noneMatch(effect -> effect.targetSpec()
                .admits(TargetPredicate.Kind.PERMANENT));
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                entry.getCard(),
                entry.getControllerId(),
                effects,
                playerTargetOnly,
                playerTargetOnly ? playerTargetFilter(effects) : null,
                0,
                entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot(),
                false,
                entry.getTriggeringPermanentId(),
                null,
                entry.getControllerId(),
                null,
                entry.getSourcePlanarObject(),
                false,
                null,
                entry.getTriggeringPermanentSnapshot(),
                null,
                entry.isMarkSourceOncePerTurnOnAcceptance(),
                null,
                producedManaColors == null ? Set.of() : producedManaColors,
                entry.getTriggeringPlayerId(),
                null,
                false));
        gameLogService.append(gameData, GameLog.abilityTriggers(entry.getCard()));
        if (!gameData.interaction.isAwaitingInput()) {
            triggerCollectionServiceProvider.getObject().processNextSpellTargetTrigger(gameData);
        }
    }

    /** The player target filter of the first effect whose target spec names a player predicate, else {@code null}. */
    private static TargetFilter playerTargetFilter(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            PlayerPredicate predicate = effect.targetSpec().targetPredicate() == null ? null
                    : effect.targetSpec().targetPredicate().leaf(TargetPredicate.Kind.PLAYER)
                    .map(leaf -> ((TargetPredicate.Players) leaf).inner())
                    .orElse(null);
            if (predicate != null) {
                return new PlayerPredicateTargetFilter(predicate, "Target must match the ability's targeting restriction");
            }
        }
        return null;
    }
}
