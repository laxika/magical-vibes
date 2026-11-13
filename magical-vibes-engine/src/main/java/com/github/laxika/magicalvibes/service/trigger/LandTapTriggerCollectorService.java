package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeredManaAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The trigger collector of the tap-for-mana watches: "whenever a player taps a land for mana" (and the abilities it
 * reads by that trigger: "you tap a land", "an opponent taps a land", "enchanted land is tapped", "you tap this land",
 * {@link TapWatch#ON_ANY_PLAYER_TAPS_LAND}) and "a player taps a permanent for mana", "you tap a creature / a nonland
 * permanent / an artifact token for mana". The caller judged the trigger, its filters, its relation and its options;
 * every ability is handed to the stack door with a record of the tap whatever its effects are ({@link #tapRequest}),
 * and is
 * <ul>
 *   <li>a triggered mana ability (no target, could add mana, CR 605.1b), resolved at once through its effects'
 *       handlers (CR 605.4a, {@link TriggeredAbilityStackService#putManaAbility}), or held until the tapped
 *       permanent's own mana choice settles the type of mana it produced when an effect reads it
 *       ({@link TriggeredAbilityStackService#holdForProducedMana});</li>
 *   <li>a targeted ability, whose target is chosen as it goes on the stack (CR 603.3d), after that mana choice when the
 *       produced mana isn't known yet;</li>
 *   <li>any other ability, put on the stack as written (it can't add mana, so it isn't a mana ability and uses the
 *       stack, CR 605.1b).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LandTapTriggerCollectorService {

    /** The trigger of the abilities this collector hands to the stack door: a permanent tapped for mana. */
    private static final TapTrigger TAPPED_FOR_MANA = Triggers.tappedForMana();

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final ObjectProvider<TriggerCollectionService> triggerCollectionServiceProvider;
    private final ObjectProvider<TriggeredAbilityStackService> stackServiceProvider;

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
    private boolean handleLandTapAbility(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.LandTap landTap = (TriggerContext.LandTap) ctx;
        return collect(match, landTap.tappingPlayerId(), landTap.tappedLandId(), landTap.producedColors(),
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
    private boolean handlePermanentTapForManaAbility(TriggerMatchContext match, TriggerContext ctx) {
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
        if (produced != null && produced.isEmpty()
                && match.effects().stream().anyMatch(LandTapTriggerCollectorService::readsProducedMana)) {
            return false;
        }
        return collect(match, tappingPlayerId, tappedPermanentId, produced, tappedPermanentId);
    }

    private boolean collect(TriggerMatchContext match, UUID tappingPlayerId,
                            UUID tappedPermanentId, Set<ManaColor> producedColors, UUID thatObjectId) {
        GameData gameData = match.gameData();
        TriggeredAbilityStackService door = stackServiceProvider.getObject();
        boolean producedUnknown = producedColors == null || (producedColors.isEmpty()
                && gameData.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice);
        if (TapAbilityReader.isTriggeredManaAbility(match.effects())) {
            if (producedUnknown && TapAbilityReader.steps(match.effects()).stream()
                    .anyMatch(LandTapTriggerCollectorService::readsProducedMana)) {
                return hold(door, match, tappingPlayerId, tappedPermanentId, thatObjectId);
            }
            return door.putManaAbility(gameData, tapRequest(match, tappingPlayerId, tappedPermanentId, thatObjectId,
                    producedColors == null ? Set.of() : producedColors), entry -> resolveAtOnce(gameData, entry));
        }
        if (producedUnknown && (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER))) {
            return hold(door, match, tappingPlayerId, tappedPermanentId, thatObjectId);
        }
        TriggeredAbilityRequest request = tapRequest(match, tappingPlayerId, tappedPermanentId, thatObjectId,
                producedColors == null ? Set.of() : producedColors);
        boolean triggered = door.put(gameData, request);
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(request.sourceCard()));
            log.info("Game {} - {} triggers on a tap for mana by {}", gameData.id, request.sourceCard().getName(),
                    gameData.playerIdToName.get(tappingPlayerId));
        }
        return triggered;
    }

    /**
     * Holds the ability of {@code match} at the stack door until the tapped permanent's own mana choice settles the
     * produced mana ({@code ChoiceHandlerService.resolveProducedManaTriggers} resolves a triggered mana ability then,
     * and {@link #putResolvedProducedManaTrigger} puts a targeted ability on the stack).
     */
    private boolean hold(TriggeredAbilityStackService door, TriggerMatchContext match, UUID tappingPlayerId,
                         UUID tappedPermanentId, UUID thatObjectId) {
        TriggeredAbilityRequest request = tapRequest(match, tappingPlayerId, tappedPermanentId, thatObjectId, null);
        boolean triggered = door.holdForProducedMana(match.gameData(), request);
        if (triggered) {
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(request.sourceCard()));
        }
        return triggered;
    }

    /**
     * The request of the ability of {@code match}'s source for a tap for mana, its effects as the reader judged them:
     * its record names the tapping player as its triggering player, the tapped permanent as its triggering permanent
     * with its last-known information, {@code thatObjectId} as its non-targeting target ("that player" of a land tap,
     * "that permanent" of a permanent's), the mana the tap produced ({@code null} while it isn't known), and the source
     * as it was when the ability triggered (a permanent, or a face-up plane). A triggered mana ability is its steps (a
     * sequence's, each resolved by its handler). A player's granted ability has no source
     * card: its source is a placeholder named for a mana ability.
     */
    private TriggeredAbilityRequest tapRequest(TriggerMatchContext match, UUID tappingPlayerId,
                                               UUID tappedPermanentId, UUID thatObjectId,
                                               Set<ManaColor> producedColors) {
        Card sourceCard = match.sourceCard() != null ? match.sourceCard()
                : match.permanent() != null ? match.permanent().getCard()
                : Card.namedRuntimePlaceholder("Mana ability");
        List<CardEffect> effects = TapAbilityReader.isTriggeredManaAbility(match.effects())
                ? TapAbilityReader.steps(match.effects()) : match.effects();
        Permanent tapped = tappedAsItLastExisted(match.gameData(), tappedPermanentId);
        Permanent sourceSnapshot = match.permanent() == null ? null : new Permanent(match.permanent());
        PlanarObject planarObject = match.sourcePlanarObject() == null ? null : match.sourcePlanarObject().copy();
        String onceEachTurnActionKey = match.onceEachTurnActionKey();
        TriggerRecord record = entry -> {
            entry.setTargetId(thatObjectId);
            entry.setNonTargeting(true);
            entry.setTriggeringPlayerId(tappingPlayerId);
            entry.setTriggeringPermanentId(tappedPermanentId);
            entry.setTriggeringPermanent(tapped);
            entry.setOnceEachTurnActionKey(onceEachTurnActionKey);
            if (sourceSnapshot != null) {
                entry.setSourcePermanentSnapshot(sourceSnapshot);
            }
            if (planarObject != null) {
                entry.setSourcePlanarObject(planarObject.copy());
            }
            if (producedColors != null) {
                entry.setProducedManaColors(producedColors);
            }
        };
        String description = match.sourceCard() == null && match.permanent() == null ? "Mana ability"
                : sourceCard.getName() + "'s ability";
        ConditionContext context = match.permanent() != null
                ? ConditionContext.forPermanent(match.permanent(), match.controllerId())
                : ConditionContext.forCard(sourceCard, match.controllerId());
        return new TriggeredAbilityRequest(new TriggeredAbility(TAPPED_FOR_MANA,
                effects.toArray(CardEffect[]::new)), sourceCard, match.permanent(), match.controllerId(),
                description, context, record);
    }

    /**
     * Resolves the triggered mana ability {@code entry} at once (CR 605.4a), each step by its handler, reading the tap
     * from the entry.
     */
    private void resolveAtOnce(GameData gameData, StackEntry entry) {
        // A step that waits for a choice takes the steps after it into a follow-up, so the entry is read afresh.
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
     * Puts a held targeted ability (C.A.M.P.: it targets, so it isn't a mana ability, CR 605.1b) on the stack once the
     * tapped permanent's mana choice settled {@code manaColor}: its target is chosen as it goes there (CR 603.3d), with
     * its record of the tap. {@code false} for a held triggered mana ability, which the caller resolves.
     */
    public boolean putResolvedProducedManaTrigger(GameData gameData, StackEntry entry, ManaColor manaColor) {
        if (entry.getEffectsToResolve().stream().noneMatch(effect -> effect.targetSpec().admits(
                TargetPredicate.Kind.PERMANENT) || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            return false;
        }
        entry.setProducedManaColors(Set.of(manaColor));
        stackServiceProvider.getObject().putHeld(gameData, entry);
        if (!gameData.interaction.isAwaitingInput()) {
            triggerCollectionServiceProvider.getObject().beginStackDoorChoices(gameData);
        }
        return true;
    }
}
