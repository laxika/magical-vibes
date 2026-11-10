package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trigger collectors for death and leaves-battlefield events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeathTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport graveyardTargetingSupport;
    @Autowired @Lazy private GraveyardTargetingService graveyardTargetingService;

    /**
     * The default of each "whenever [an object] dies" collector the trigger pipeline puts an ability on the stack
     * through ({@link CreatureDeathWatch}): it records the death whatever the effects are, so no handler is picked by
     * an effect's class (effect-slot-refactor.md step 15, run 17z13e4).
     */
    private final Map<CreatureDeathWatch, TriggerCollectorHandler> creatureDeathDefaults =
            new EnumMap<>(CreatureDeathWatch.class);

    {
        creatureDeathDefaults.put(CreatureDeathWatch.EQUIPPED, this::handleEquippedCreatureDeathDefault);
        creatureDeathDefaults.put(CreatureDeathWatch.ALLY_ARTIFACT_OR_CREATURE, this::handleAllyArtifactOrCreatureDeath);
        creatureDeathDefaults.put(CreatureDeathWatch.ANY, this::handleAnyCreatureDeathDefault);
        creatureDeathDefaults.put(CreatureDeathWatch.ALLY_NONTOKEN, this::handleAllyNontokenDefault);
        creatureDeathDefaults.put(CreatureDeathWatch.ANY_NONTOKEN, this::handleAnyNontokenCreatureDeathDefault);
        creatureDeathDefaults.put(CreatureDeathWatch.ENCHANTED_PLAYER_NONTOKEN,
                this::handleEnchantedPlayerNontokenCreatureDeathDefault);
        creatureDeathDefaults.put(CreatureDeathWatch.OPPONENT, this::handleOpponentCreatureDeathDefault);
    }

    /**
     * Puts {@code match}'s ability, a "whenever [an object] dies" ability that {@code watch}'s collector reads, on the
     * stack through the watch's default, whole (CR 603.3). A "you may" is the effect itself, never unwrapped, and is
     * asked as the ability resolves (CR 603.5). Returns whether it triggered.
     */
    public boolean dispatchCreatureDeath(CreatureDeathWatch watch, TriggerMatchContext match,
                                         TriggerContext context) {
        TriggerCollectorHandler handler = creatureDeathDefaults.get(watch);
        return handler != null && handler.handle(match, context);
    }

    /**
     * The mana value limit of the graveyard target of {@code effects} ({@code ReturnCardFromGraveyardEffect
     * .dynamicMaxManaValue()}), evaluated as the ability triggers with {@code lastKnown}, the object that died as it
     * last existed, as the amount's source, so it looks back at the others that died with it (CR 603.10a): Soulshift X
     * counts the Kodama, "with lesser mana value" is the dying object's. The choice carries it to the stack entry.
     * {@code null} when none has such a limit.
     */
    private Integer graveyardTargetMaxManaValueAtDeath(GameData gameData, UUID controllerId, List<CardEffect> effects,
                                                       Permanent lastKnown) {
        var target = graveyardTargetingSupport.findTarget(effects);
        if (target == null || target.maximumManaValue() == null || lastKnown == null) {
            return null;
        }
        return amountEvaluationService.evaluateAtDeath(gameData, target.maximumManaValue(), controllerId, lastKnown);
    }

    /**
     * "Whenever equipped creature dies": the Equipment's ability (CR 113.7) records the creature that died whatever
     * the effects are ({@link #queueDeathAbility}); a target is chosen within the Equipment's declared filter.
     */
    boolean handleEquippedCreatureDeathDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath equipped = (TriggerContext.EquippedCreatureDeath) ctx;
        Permanent dying = equipped.dyingPermanent();
        int toughness = dying == null ? 0
                : dying.getLastKnownToughness() != null ? dying.getLastKnownToughness() : dying.getEffectiveToughness();
        var death = new TriggerContext.CreatureDeath(equipped.dyingCard(), equipped.dyingCreatureControllerId(),
                equipped.dyingCreaturePower(), toughness, equipped.dyingCreatureId(), dying);
        queueDeathAbility(match, death, null, match.permanent().getCard().getTargetFilter(), true);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} triggers (equipped creature died)", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedPermanentDeathDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        addEnchantedPermanentDeathEntryReturning(match, ctx, null, epd.dyingPermanentControllerId());
        return true;
    }

    /**
     * Puts an "enchanted permanent is put into a graveyard" ability on the stack; returns the stack entry, or
     * {@code null} if the trigger was queued for a target choice. The entry (or the choice) records the permanent
     * that was put into a graveyard as its triggering permanent, as it last existed on the battlefield (CR 603.10a):
     * "its controller", "its power", "its toughness" and "its owner" are read from it as the ability resolves, never
     * bound into the effect. The entry's triggering cards are the cards that permanent put into the graveyard, each
     * with its graveyard incarnation (CR 400.7), which "return that card" reads. Its event value is that creature's
     * last-known power ("X is its power") unless {@code eventValue} says otherwise.
     */
    private StackEntry addEnchantedPermanentDeathEntryReturning(TriggerMatchContext match, TriggerContext ctx,
            Integer eventValue, UUID enchantedControllerId) {
        TriggerContext.EnchantedPermanentDeath epd = ctx instanceof TriggerContext.EnchantedPermanentDeath death
                ? death : null;
        if (eventValue == null && epd != null) {
            eventValue = Math.max(0, epd.dyingCreaturePower());
        }
        if (match.admits(TargetPredicate.Kind.PERMANENT) || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(), eventValue,
                    new Permanent(match.permanent()), false);
            match.gameData().queueInteraction(epd == null || epd.dyingPermanent() == null ? target
                    : target.withTriggeringPermanent(epd.dyingPermanent())
                            .withTriggeringPermanentController(epd.dyingPermanentControllerId()));
            return null;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        // The Aura is still on the battlefield as its ability triggers and is put into its owner's graveyard by the
        // state-based actions that follow (CR 704.5m): that stay is the one "return this card" means, whatever the
        // effects are, and GameData.markGraveyardEntry records it on this entry as the Aura arrives (CR 400.7).
        entry.setAwaitingSourceGraveyardEntry(true);
        if (enchantedControllerId != null) {
            entry.setTargetId(enchantedControllerId);
            entry.setNonTargeting(true);
        }
        // The field is a primitive, so a null would unbox and throw.
        if (eventValue != null) {
            entry.setEventValue(eventValue);
        }
        if (epd != null) {
            recordEnchantedPermanent(match.gameData(), entry, epd);
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id, match.permanent().getCard().getName());
        return entry;
    }

    /**
     * Records the enchanted permanent that was put into a graveyard as {@code entry}'s triggering permanent: its
     * last-known snapshot, controller, owner, power and toughness (CR 603.10a). The effects read them as the ability
     * resolves ({@code TRIGGERING_PERMANENT_CONTROLLER} recipients, {@code TriggeringPermanentPower} /
     * {@code TriggeringPermanentToughness} amounts). The cards it put into the graveyard (every card of a merged
     * permanent) are the entry's triggering cards, each with the graveyard incarnation it has now (CR 400.7).
     */
    private static void recordEnchantedPermanent(GameData gameData, StackEntry entry,
                                                 TriggerContext.EnchantedPermanentDeath epd) {
        entry.setTriggeringPermanentId(epd.dyingPermanentId());
        if (epd.dyingPermanent() != null) {
            entry.setTriggeringPermanent(epd.dyingPermanent());
        }
        entry.setTriggeringPermanentControllerId(epd.dyingPermanentControllerId());
        entry.setTriggeringPermanentOwnerId(epd.dyingPermanentOwnerId());
        entry.setTriggeringPermanentPowerAtTrigger(epd.dyingCreaturePower());
        entry.setTriggeringPermanentToughnessAtTrigger(epd.dyingCreatureToughness());
        List<Card> dyingCards = epd.dyingPermanent() != null ? epd.dyingPermanent().cardsLeavingBattlefield()
                : epd.dyingCard() != null ? List.of(epd.dyingCard()) : List.of();
        entry.setTriggeringCards(dyingCards);
        for (Card card : dyingCards) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
    }

    /**
     * "When enchanted creature leaves the battlefield": the entry records the permanent that left as its triggering
     * permanent, as it last existed on the battlefield, and its controller then (CR 603.10a), which "its controller"
     * reads as the ability resolves (Funeral March's sacrifice, Traveling Plague's return). When it was put into a
     * graveyard or exiled its card is the entry's triggering card, with its incarnation in that zone (CR 400.7), which
     * "return it" reads (Kaya's Ghostform).
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedPermanentLeavesDefault(TriggerMatchContext match, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects()
        );
        if (ctx instanceof TriggerContext.EnchantedPermanentLeaves leaves && leaves.leavingPermanent() != null) {
            entry.setTriggeringPermanentId(leaves.leavingPermanent().getId());
            entry.setTriggeringPermanent(leaves.leavingPermanent());
            entry.setTriggeringPermanentControllerId(leaves.leavingControllerId());
            UUID cardId = leaves.leavingPermanent().getCard().getId();
            if (leaves.destination() == com.github.laxika.magicalvibes.model.Zone.GRAVEYARD) {
                entry.setTriggeringCard(cardId, match.gameData().graveyardEntryVersion(cardId));
            } else if (leaves.destination() == com.github.laxika.magicalvibes.model.Zone.EXILE) {
                entry.setTriggeringCardId(cardId);
                entry.setTriggeringCardExileEntryVersion(
                        match.gameData().exileEntryVersions.getOrDefault(cardId, 0L));
            }
        }
        match.gameData().stack.add(entry);
        logEnchantedPermanentLTB(match);
        return true;
    }

    private void logEnchantedPermanentLTB(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent left the battlefield)."));
        log.info("Game {} - {} triggers (enchanted permanent left the battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    /**
     * "Whenever an artifact [you control] is put into a graveyard from the battlefield": the departure recorded whatever
     * the effects are ({@link #queueDepartureAbility}), the artifact as it last existed on the battlefield (CR 603.10a)
     * the source of a graveyard target's mana value limit.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        queueDepartureAbility(match, ag.artifactCard(), ag.artifactControllerId(), ag.artifactPermanent(), null);
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD)
    boolean handleArtifactExiledDefault(TriggerMatchContext match, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (artifact exiled from battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    private void logArtifactGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    /**
     * "Whenever a land is put into a graveyard from the battlefield": the departure recorded whatever the effects are
     * ({@link #queueDepartureAbility}); "that land's controller" (Dingus Egg, Thendar, the Overminer) is its
     * controller as it last existed.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleLandGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueDepartureAbility(match, (TriggerContext.AnyPermanentGraveyard) ctx, null);
        logLandGraveyard(match);
        return true;
    }

    /**
     * "Whenever a spell or ability an opponent controls causes a land to be put into your graveyard from the
     * battlefield" (Sacred Ground): the departure recorded whatever the effects are ({@link #queueDepartureAbility}),
     * "that card" the land card with the graveyard incarnation it has now (CR 400.7).
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT)
    boolean handleLandPutIntoGraveyardByOpponentDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueDepartureAbility(match, (TriggerContext.AnyPermanentGraveyard) ctx, null);
        logLandGraveyard(match);
        return true;
    }

    private void logLandGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleEnchantmentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        logEnchantmentGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyEnchantmentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        logEnchantmentGraveyard(match);
        return true;
    }

    private void logEnchantmentGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (enchantment put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    /**
     * "Whenever an artifact an opponent controls is put into a graveyard from the battlefield": the departure recorded
     * whatever the effects are ({@link #queueDepartureAbility}), the artifact's controller its non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        queueDepartureAbility(match, ag.artifactCard(), ag.artifactControllerId(), ag.artifactPermanent(),
                ag.artifactControllerId());
        logOpponentArtifactGraveyard(match);
        return true;
    }

    /**
     * "Whenever an artifact is put into an opponent's graveyard from the battlefield": the departure recorded whatever
     * the effects are ({@link #queueDepartureAbility}), the graveyard's owner its non-targeting "that player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentOwnerArtifactGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        queueDepartureAbility(match, ag.artifactCard(), ag.artifactControllerId(), ag.artifactPermanent(),
                ag.graveyardOwnerId());
        logOpponentArtifactGraveyard(match);
        return true;
    }

    private void logOpponentArtifactGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    /**
     * "Whenever a permanent an opponent controls is put into a graveyard": the departure recorded whatever the effects
     * are ({@link #queueDepartureAbility}), "that opponent", the permanent's controller as it last existed, its
     * non-targeting "that player" (Prince of Thralls).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentPermanentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        queueDepartureAbility(match, apg, apg.dyingControllerId());
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever a spell or ability an opponent controls destroys a noncreature permanent you control" (Karmic Justice):
     * the departure recorded whatever the effects are ({@link #queueDepartureAbility}).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT)
    boolean handleNoncreaturePermanentDestroyedByOpponentDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueDepartureAbility(match, (TriggerContext.AnyPermanentGraveyard) ctx, null);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers after an opponent destroys a noncreature permanent",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * "Whenever a noncreature artifact is sacrificed or destroyed" (Jaws, Relentless Predator): the departure recorded
     * whatever the effects are ({@link #queueDepartureAbility}).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED)
    boolean handleNoncreatureArtifactSacrificedOrDestroyedDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueDepartureAbility(match, (TriggerContext.AnyPermanentGraveyard) ctx, null);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on a noncreature artifact being sacrificed or destroyed",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * "Whenever a creature is put into your graveyard from the battlefield": the death recorded whatever the effects
     * are ({@link #queueDeathAbility}), "that card" or "it" the card with its graveyard incarnation (CR 400.7).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleCreaturePutIntoOwnerGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        queueDeathAbility(match, departure(apg.dyingCard(), apg.dyingControllerId(), apg.dyingPermanent()),
                null, null, true);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever a permanent [you control] is put into a graveyard from the battlefield": the departure recorded
     * whatever the effects are ({@link #queueDepartureAbility}), the owner of the graveyard its non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyPermanentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        queueDepartureAbility(match, apg, apg.graveyardOwnerId());
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }boolean handleAllyArtifactOrCreatureDeath(TriggerMatchContext match, TriggerContext ctx) {
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (controlled artifact or creature put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyNontokenPermanentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        );
        // The graveyard's owner is "that permanent's owner", not a target of the ability.
        entry.setNonTargeting(true);
        markDyingCard(match.gameData(), entry, apg.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (nontoken permanent put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyNontokenPermanentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, ((TriggerContext.AnyPermanentGraveyard) ctx).dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (nontoken permanent put into controller's graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * "Whenever another permanent you control is put into a graveyard from the battlefield" and "whenever a permanent is
     * put into your graveyard from the battlefield": the departure recorded whatever the effects are
     * ({@link #queueDepartureAbility}), "it" the permanent as it last existed (Martyr's Bond).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyPermanentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueDepartureAbility(match, (TriggerContext.AnyPermanentGraveyard) ctx, null);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent put into controller's graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * Patron of the Nezumi. "That player" is the owner of the graveyard the permanent was put into,
     * so the entry's target is baked to the graveyard owner rather than the dying permanent's
     * controller — a permanent an opponent owns but you control still hits its owner.
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handlePermanentIntoOpponentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        );
        // "Its toughness" (Grim Feast): the permanent as it last existed on the battlefield.
        markDyingCard(match.gameData(), entry, apg.dyingCard());
        entry.setTriggeringPermanent(apg.dyingPermanent());
        entry.setTriggeringPermanentToughnessAtTrigger(apg.dyingToughness());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent {} put into an opponent's graveyard)",
                match.gameData().id, match.permanent().getCard().getName(), apg.dyingCard().getName());
        return true;
    }

    /** "Whenever a creature dies": the death recorded whatever the effects are ({@link #queueDeathAbility}). */
    boolean handleAnyCreatureDeathDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueDeathAbility(match, (TriggerContext.CreatureDeath) ctx, null, null, true);
        logCreatureDeath(match, "any creature died");
        return true;
    }

    /**
     * "Whenever a nontoken creature you control dies": the death recorded whatever the effects are
     * ({@link #queueDeathAbility}).
     */
    boolean handleAllyNontokenDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueDeathAbility(match, (TriggerContext.CreatureDeath) ctx, null, null, true);
        logCreatureDeath(match, "ally nontoken creature died");
        return true;
    }

    /**
     * "Whenever a creature an opponent controls dies": the death recorded whatever the effects are
     * ({@link #queueDeathAbility}), the creature's controller as the entry's non-targeting "that player".
     */
    boolean handleOpponentCreatureDeathDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        queueDeathAbility(match, death, death.dyingCreatureControllerId(), null, true);
        logCreatureDeath(match, "opponent creature died");
        return true;
    }

    /**
     * Puts {@code match}'s ability, a "whenever [a creature] dies" ability, on the stack for {@code death} as one
     * object (CR 603.3), whatever its effects are. The entry records the card that went to the graveyard ("that card",
     * with the graveyard incarnation it has now, CR 400.7), the creature as it last existed on the battlefield
     * with what was attached to it, its controller, power, toughness and mana value then ("it", "its controller",
     * "that creature's power", CR 603.10a and 608.2h), its power as the event value, the source as it triggered and
     * the "do this only once each turn" mark (CR 603.2h). A "you may" is the effect and is asked as it resolves
     * (CR 603.5). An ability that targets waits for its targets as it goes on the stack (CR 603.3d), its choice
     * carrying every effect and the same death.
     *
     * @param thatPlayerId the player the entry names as its non-targeting "that player", or {@code null}
     * @param targetFilter the source's declared target filter for the choice, or {@code null} for the effect's own
     */
    private void queueDeathAbility(TriggerMatchContext match, TriggerContext.CreatureDeath death,
                                   UUID thatPlayerId, TargetFilter targetFilter, boolean creaturesOnly) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        Card sourceCard = source != null ? source.getCard() : match.sourceCard();
        Card graveyardCard = graveyardCardOf(death);
        if (targets(match)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    death.dyingCreaturePower(), source == null ? null : new Permanent(source), targetFilter, null,
                    creaturesOnly, match.markSourceOncePerTurnOnAcceptance())
                    .withTriggeringPermanent(death.dyingPermanent(), death.attachments())
                    .withTriggeringPermanentController(death.dyingCreatureControllerId())
                    .withGraveyardTargetMaxManaValue(graveyardTargetMaxManaValueAtDeath(
                            gameData, match.controllerId(), match.effects(), death.dyingPermanent()));
            gameData.queueInteraction(graveyardCard == null ? target : target.withTriggeringCard(
                    graveyardCard.getId(), gameData.graveyardEntryVersion(graveyardCard.getId())));
            return;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                thatPlayerId,
                source == null ? null : source.getId());
        entry.setNonTargeting(true);
        // The source as it triggered: "enchanted creature" of an Aura that died with the creature (Yoke of the
        // Damned, Sadistic Glee) is read from it once the Aura is gone (CR 608.2h).
        if (source != null) {
            entry.setSourcePermanentSnapshot(new Permanent(source));
        }
        entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
        recordDeath(gameData, entry, death);
        gameData.enqueueTrigger(entry);
    }

    /**
     * Puts {@code match}'s ability, one a permanent's leaving the battlefield triggered ("whenever an artifact is put
     * into a graveyard from the battlefield", "whenever a player sacrifices a creature", "whenever a noncreature
     * permanent you control is destroyed"), on the stack whole whatever its effects are, as
     * {@link #queueDeathAbility} records a death: "that card" is {@code card} with the graveyard or exile incarnation it
     * has now (CR 400.7), "it", "its controller", "its power" the permanent {@code lastKnown} as it last existed on the
     * battlefield (CR 603.10a, 608.2h), {@code controllerId} its controller then. {@code thatPlayerId} is the entry's
     * non-targeting "that player", or {@code null}. A target is chosen among permanents of any type the ability allows.
     * The source is the matched permanent, or the card of an ability that functions from another zone.
     */
    public void queueDepartureAbility(TriggerMatchContext match, Card card, UUID controllerId,
                                      Permanent lastKnown, UUID thatPlayerId) {
        queueDeathAbility(match, departure(card, controllerId, lastKnown), thatPlayerId, null, false);
    }

    private void queueDepartureAbility(TriggerMatchContext match, TriggerContext.AnyPermanentGraveyard departed,
            UUID thatPlayerId) {
        queueDepartureAbility(match, departed.dyingCard(), departed.dyingControllerId(),
                departed.dyingPermanent(), thatPlayerId);
    }

    /**
     * The departure of {@code lastKnown} (the permanent as it last existed on the battlefield, else {@code null}) whose
     * card {@code card} left, controlled by {@code controllerId}, as a death record: its power and toughness as it last
     * existed.
     */
    private static TriggerContext.CreatureDeath departure(Card card, UUID controllerId, Permanent lastKnown) {
        if (lastKnown == null) {
            return new TriggerContext.CreatureDeath(card, controllerId, 0, 0, null, null, false);
        }
        int power = lastKnown.getLastKnownPower() != null ? lastKnown.getLastKnownPower()
                : lastKnown.getEffectivePower();
        int toughness = lastKnown.getLastKnownToughness() != null ? lastKnown.getLastKnownToughness()
                : lastKnown.getEffectiveToughness();
        return new TriggerContext.CreatureDeath(lastKnown.getCard(), controllerId, power, toughness,
                lastKnown.getId(), lastKnown, false);
    }

    /** Whether {@code match}'s ability chooses a target as it goes on the stack (CR 603.3d). */
    private boolean targets(TriggerMatchContext match) {
        return match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                || graveyardTargetingSupport.findTarget(match.effects()) != null;
    }

    /**
     * Records {@code death} on {@code entry}: the card that went to the graveyard as its triggering card, the creature
     * as it last existed on the battlefield (CR 603.10a) as its triggering permanent with its attachments, controller,
     * power, toughness and mana value, and its power as the event value.
     */
    private static void recordDeath(GameData gameData, StackEntry entry, TriggerContext.CreatureDeath death) {
        Card graveyardCard = graveyardCardOf(death);
        markDyingCard(gameData, entry, graveyardCard);
        if (graveyardCard != null) {
            // A replacement effect may have exiled the card instead (Leyline of the Void): its exile incarnation.
            entry.setTriggeringCardExileEntryVersion(
                    gameData.exileEntryVersions.getOrDefault(graveyardCard.getId(), 0L));
        }
        if (death.dyingPermanentId() != null && death.dyingCard() != null) {
            entry.rememberLastKnownPermanentCard(death.dyingPermanentId(), death.dyingCard());
        }
        entry.setTriggeringPermanentId(death.dyingPermanentId());
        entry.setTriggeringPermanent(death.dyingPermanent(), death.attachments());
        entry.setTriggeringPermanentControllerId(death.dyingCreatureControllerId());
        entry.setTriggeringPermanentPowerAtTrigger(death.dyingCreaturePower());
        entry.setTriggeringPermanentToughnessAtTrigger(death.dyingCreatureToughness());
        entry.setEventValue(death.dyingCreaturePower());
        Card lastKnownCard = death.dyingPermanent() != null ? death.dyingPermanent().getCard() : death.dyingCard();
        if (lastKnownCard != null) {
            entry.setDyingPermanentManaValue(lastKnownCard.getManaValue());
        }
    }

    /**
     * The card {@code death} put into the graveyard: the permanent's physical card (a copy's own card, a transformed
     * permanent's front face), else the dying card.
     */
    private static Card graveyardCardOf(TriggerContext.CreatureDeath death) {
        return death.dyingPermanent() != null && death.dyingPermanent().getOriginalCard() != null
                ? death.dyingPermanent().getOriginalCard() : death.dyingCard();
    }

    private void logCreatureDeath(TriggerMatchContext match, String event) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers ({})", match.gameData().id, match.permanent().getCard().getName(), event);
    }

    boolean handleAnyNontokenCreatureDeathDefault(TriggerMatchContext match, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        if (ctx instanceof TriggerContext.CreatureDeath death) {
            markDyingCard(match.gameData(), entry, death.dyingCard());
            entry.setTriggeringPermanent(death.dyingPermanent());
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (any nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleEnchantedPlayerNontokenCreatureDeathDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        if (death.dyingCard() != null) {
            entry.setDyingPermanentManaValue(death.dyingCard().getManaValue());
        }
        markDyingCard(match.gameData(), entry, death.dyingCard());
        entry.setTriggeringPermanent(death.dyingPermanent());
        match.gameData().stack.add(entry);
        log.info("Game {} - {} triggers (enchanted player's nontoken creature died)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * "Whenever an Aura or Equipment you control is put into a graveyard from the battlefield": "that card" is the
     * entry's triggering card. An ability that watches only Auras (Mazzy, Truesword Paladin) says so in its trigger
     * filter, which the pipeline judges on the departed permanent.
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyAuraOrEquipmentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AllyAuraOrEquipmentGraveyard aaeg = (TriggerContext.AllyAuraOrEquipmentGraveyard) ctx;
        Card dyingCard = aaeg.dyingCard();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        markDyingCard(match.gameData(), entry, dyingCard);
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", dyingCard, " was put into a graveyard from the battlefield)."));
        log.info("Game {} - {} triggers (Aura or Equipment {} put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName(), dyingCard.getName());
        return true;
    }

    /**
     * Gives {@code entry} the card that died ("that card", "it") as its triggering card, with the graveyard
     * incarnation it has now (CR 400.7), for the effects that read it as they resolve; nothing for {@code null}.
     */
    private static void markDyingCard(GameData gameData, StackEntry entry, Card dyingCard) {
        if (dyingCard != null) {
            entry.setTriggeringCard(dyingCard.getId(), gameData.graveyardEntryVersion(dyingCard.getId()));
        }
    }
}
