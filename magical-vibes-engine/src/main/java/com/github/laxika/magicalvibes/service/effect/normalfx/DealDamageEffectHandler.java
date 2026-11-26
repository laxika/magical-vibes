package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect.ExileIfItWouldDie;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect.Recipient;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayExileOneAndPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link DealDamageEffect}. Every targeted recipient is dealt the damage through one permanent-damage path,
 * {@link DamageSupport#resolveAnyTargetDamage} (every prevention is an option of the recipient's damage pool, ordered
 * by its controller, CR 616.1), with one dealer, one unpreventable flag, one excess-damage tracker and the DEAL_DAMAGE
 * step begun for every kind (CR 608.2c).
 *
 * <ul>
 *   <li><b>Targets.</b> The any-target, player-or-planeswalker and permanent kinds deal to one target: the entry's
 *       target, or the first of target group {@code targetGroup}'s. The creature and creature-or-planeswalker kinds
 *       deal to each of the effect's targets ("each of up to two target creatures", Dual Shot) when it has several
 *       or none is the entry's own. The any-target kind can't take that loop: an unbound any-target part of a
 *       two-target spell (Magma Burst kicked) would be dealt to the other part's target too.</li>
 *   <li><b>Dealer.</b> The ability's source, or with {@link DamageSource#TRIGGERING_PERMANENT} the permanent the
 *       trigger event was about, as it is or most recently existed (CR 608.2h), through
 *       {@link DamageSupport#entryDealtByTriggeringPermanent}: its lifelink, deathtouch and infect apply. The amount
 *       is evaluated against the dealer.</li>
 *   <li><b>Excess</b> (CR 120.4a, 120.10): written to the entry's event value when a later step of the entry reads it
 *       and the amount doesn't itself read the event value: over lethal damage for a creature (1 with the dealer's
 *       deathtouch), over loyalty for a planeswalker (its toughness when that is its loyalty), over defense for a
 *       battle, the greatest for several types.</li>
 *   <li><b>Players</b> ({@link Recipient#PLAYERS}, {@link Recipient#PLAYER_OR_THEIR_PLANESWALKER}): see
 *       {@link #damagePlayers}.</li>
 *   <li><b>Non-target subjects</b> (creature kind): {@code source()} through the same path; {@code triggering()} and
 *       {@code thisWay(kind)} with the targeted-creature prevention check; the Aura subjects with the Aura, as it is
 *       or last existed, dealing the damage.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class DealDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The damage this step deals is added to its result as each damage event is dealt (DamageSupport).
        entry.recordStep(StepKind.DEAL_DAMAGE, StepOutcome.NONE);
        DealDamageEffect e = (DealDamageEffect) effect;
        if (e.recipient().namesPlayers()) {
            damagePlayers(gameData, entry, e);
            return;
        }
        if (e.recipient() == Recipient.EACH_PERMANENT) {
            damageEachPermanent(gameData, entry, e);
            return;
        }
        if (e.recipient() == Recipient.EACH_TARGET) {
            damageEachTarget(gameData, entry, e);
            return;
        }
        switch (e.subject()) {
            case PermanentSubject.Source ignored -> damageSource(gameData, entry, e);
            case PermanentSubject.Triggering ignored -> damageTriggeringPermanent(gameData, entry, e);
            case PermanentSubject.Attached ignored -> damageAuraHost(gameData, entry, e);
            case PermanentSubject.AttachedAtTrigger ignored -> damageAuraHost(gameData, entry, e);
            case PermanentSubject.ThisWay ignored -> damageNamedThisWay(gameData, entry, e);
            default -> damageTargets(gameData, entry, e);
        }
    }

    private void damageTargets(GameData gameData, StackEntry entry, DealDamageEffect e) {
        if (e.recordDamageDealt()) {
            entry.setEventValue(0);
        }

        StackEntry dealing = entry;
        Permanent dealer;
        if (e.damageSource() == DamageSource.TRIGGERING_PERMANENT) {
            dealing = damageSupport.entryDealtByTriggeringPermanent(gameData, entry);
            if (dealing == null) {
                return;
            }
            dealing.setTriggeringPermanentId(entry.getTriggeringPermanentId());
            dealer = gameQueryService.findPermanentById(gameData, dealing.getSourcePermanentId());
            if (dealer == null) {
                dealer = dealing.getSourcePermanentSnapshot();
            }
        } else {
            dealer = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            if (dealer == null) {
                dealer = entry.getSourcePermanentSnapshot();
            }
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(), AmountContext.forStackEntry(entry, dealer));

        List<UUID> targets = targetsOf(gameData, entry, e);
        if (targets.isEmpty()) {
            return;
        }
        boolean single = targets.size() == 1;

        if (e.preventRegenerationWithoutDamage() || e.exile() == ExileIfItWouldDie.TARGET) {
            for (UUID targetId : targets) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                boolean anyTargetCreature = gameQueryService.isCreature(gameData, target);
                if (e.preventRegenerationWithoutDamage() && anyTargetCreature) {
                    gameData.addEffectOnPermanent(entry, new PreventTargetCreatureRegenerationThisTurnEffect(),
                            target.getId(), Duration.UNTIL_END_OF_TURN);
                }
                // "If that creature would die this turn": marked whether or not it is dealt damage.
                if (e.exile() == ExileIfItWouldDie.TARGET
                        && (e.recipient() != Recipient.ANY_TARGET || anyTargetCreature)) {
                    gameData.exileInsteadOfDying(entry, target.getId());
                }
            }
        }

        ExcessBaseline excess = single && tracksExcess(entry, e)
                ? excessBaseline(gameData, dealing, targets.getFirst())
                : null;
        Map<UUID, Integer> damageBefore = e.exile() == ExileIfItWouldDie.DAMAGED_PERMANENT
                ? new HashMap<>(gameData.damageDealtToPermanentsThisTurn) : Map.of();
        UUID damageSourceId = dealing.getSourcePermanentId() != null
                ? dealing.getSourcePermanentId() : dealing.getEffectiveDamageSourceCard().getId();
        int dealtBySourceBefore = gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0);

        // "The damage can't be prevented" (CR 615.12) holds for this one damage event: the flag is raised only around
        // dealing it and always dropped again.
        boolean unpreventable = e.alwaysUnpreventable() || e.unpreventableWhen() != null
                && conditionEvaluationService.isMet(gameData, e.unpreventableWhen(), ConditionContext.forStackEntry(entry));
        boolean cantRegenerate = e.cantRegenerate() && !e.preventRegenerationWithoutDamage();
        boolean cantBeRedirected = unpreventable && e.cantBeRedirectedWhenUnpreventable();
        boolean previousUnpreventable = gameData.unpreventableDamageInProgress;
        boolean previousExilesCreaturesDamaged = dealing.isExilesCreaturesDamaged();
        dealing.setExilesCreaturesDamaged(e.exile() == ExileIfItWouldDie.DAMAGED_CREATURE);
        gameData.unpreventableDamageInProgress = previousUnpreventable || unpreventable;
        int damageDealt = 0;
        try {
            for (UUID targetId : targets) {
                damageDealt = damageSupport.resolveAnyTargetDamage(gameData, dealing, targetId, damage, cantRegenerate,
                        cantBeRedirected);
            }
        } finally {
            gameData.unpreventableDamageInProgress = previousUnpreventable;
            dealing.setExilesCreaturesDamaged(previousExilesCreaturesDamaged);
        }
        if (dealing != entry) {
            entry.addToStep(StepKind.DEAL_DAMAGE, dealing.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
        }

        if (e.exile() == ExileIfItWouldDie.DAMAGED_PERMANENT) {
            markDamagedPermanentsForExile(gameData, entry, damageBefore);
        }
        if (e.recordDamageDealt()) {
            entry.setEventValue(gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0)
                    - dealtBySourceBefore);
        }
        if (excess != null) {
            entry.setEventValue(excess.excessOf(damageSupport, damageDealt));
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * The recipients of a targeted damage effect: the any-target kind's one target (its group's when it names one);
     * the player-or-planeswalker and permanent kinds' entry target; the creature kinds' each target of the effect
     * when there are several or the entry has no single target, else its one target creature. A permanent that can't
     * be dealt damage (CR 120.1) and a player for a creature kind are left out.
     */
    private List<UUID> targetsOf(GameData gameData, StackEntry entry, DealDamageEffect e) {
        List<UUID> candidates = switch (e.recipient()) {
            case ANY_TARGET -> {
                UUID targetId = entry.isTargetIdOverriddenForEffectResolution() || e.targetGroup() < 0
                        ? entry.getTargetId()
                        : entry.targetsForGroup(e.targetGroup()).stream().findFirst().orElse(null);
                yield targetId == null ? List.of() : List.of(targetId);
            }
            case PLAYER_OR_PLANESWALKER, PERMANENT ->
                    entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
            case CREATURE, CREATURE_OR_PLANESWALKER -> {
                List<UUID> effectTargets = entry.targetsForEffect(e);
                if (effectTargets != null && !effectTargets.isEmpty()
                        && (effectTargets.size() > 1 || entry.getTargetId() == null)) {
                    yield effectTargets;
                }
                yield entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
            }
            case PLAYERS, PLAYER_OR_THEIR_PLANESWALKER, EACH_PERMANENT, EACH_TARGET ->
                    throw new IllegalStateException("Not one target's damage: " + e.recipient());
        };
        boolean playersAllowed = e.recipient() == Recipient.ANY_TARGET
                || e.recipient() == Recipient.PLAYER_OR_PLANESWALKER;
        List<UUID> recipients = new ArrayList<>(candidates.size());
        for (UUID targetId : candidates) {
            if (gameData.playerIds.contains(targetId)) {
                if (playersAllowed) recipients.add(targetId);
                continue;
            }
            Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
            if (permanent != null && damageSupport.isAnyTargetDamageRecipient(gameData, permanent)) {
                recipients.add(targetId);
            }
        }
        return recipients;
    }

    /**
     * Whether a later step of the entry reads this effect's excess damage from the event value: a condition on it,
     * or an amount of it ("that much", "the excess"). Not when the damage amount itself is the event value, nor when
     * the effect records the damage dealt instead.
     */
    private boolean tracksExcess(StackEntry entry, DealDamageEffect e) {
        return !e.recordDamageDealt()
                && !amountEvaluationService.referencesEventValue(e.damage())
                && entry.getEffectsToResolve().stream().anyMatch(this::readsExcessDamage);
    }

    private boolean readsExcessDamage(CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional) {
            return conditional.condition() instanceof EventValueAtLeast
                    || readsExcessDamage(conditional.wrapped());
        }
        if (effect instanceof LookAtTopCardsMayExileOneAndPlayThisTurnEffect look) {
            return amountEvaluationService.referencesEventValue(look.count());
        }
        if (effect instanceof ExileTopCardMayPlayThisTurnEffect exile) {
            return amountEvaluationService.referencesEventValue(exile.count());
        }
        if (effect instanceof BoostEffect boost && (boost.subject() instanceof PermanentSubject.TriggeringAttackers
                || boost.subject() instanceof PermanentSubject.ControlledBy set
                        && set.players() instanceof PlayerSubject.You)) {
            return amountEvaluationService.referencesEventValue(boost.powerBoost())
                    || amountEvaluationService.referencesEventValue(boost.toughnessBoost());
        }
        if (effect instanceof DealDamageEffect playerDamage && playerDamage.recipient() == Recipient.PLAYERS) {
            return Subjects.controllerOf(Subjects.target()).equals(playerDamage.players())
                    && amountEvaluationService.referencesEventValue(playerDamage.damage());
        }
        if (effect instanceof GainLifeEffect gainLife) {
            return amountEvaluationService.referencesEventValue(gainLife.amount());
        }
        return effect instanceof CreateTokenEffect createToken
                && amountEvaluationService.referencesEventValue(createToken.amount());
    }

    /** What excess damage to a permanent is measured against, as it was before the damage (CR 120.10). */
    private record ExcessBaseline(boolean creature, int lethalDamageThreshold, int markedDamage, boolean deathtouch,
                                  boolean planeswalker, int loyalty, boolean battle, int defense) {

        int excessOf(DamageSupport damageSupport, int damageDealt) {
            return damageSupport.computeExcessDamageToAnyTarget(damageDealt, creature, lethalDamageThreshold,
                    markedDamage, deathtouch, planeswalker, loyalty, battle, defense);
        }
    }

    /** The baseline of {@code targetId}; a player is dealt no excess damage (all zero). */
    private ExcessBaseline excessBaseline(GameData gameData, StackEntry dealing, UUID targetId) {
        Permanent target = gameData.playerIds.contains(targetId)
                ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return new ExcessBaseline(false, 0, 0, false, false, 0, false, 0);
        }
        boolean creature = gameQueryService.isCreature(gameData, target);
        boolean planeswalker = target.getCard().hasType(CardType.PLANESWALKER)
                || gameQueryService.isPlaneswalker(gameData, target);
        int loyalty = planeswalker && gameQueryService.isToughnessAsLoyaltyPermanent(gameData, target)
                ? gameQueryService.getEffectiveToughness(gameData, target)
                : target.getCounterCount(CounterType.LOYALTY);
        return new ExcessBaseline(creature,
                creature ? gameQueryService.getLethalDamageThreshold(gameData, target) : 0,
                target.getMarkedDamage(),
                gameQueryService.sourceHasKeyword(gameData, dealing, null, Keyword.DEATHTOUCH),
                planeswalker, loyalty,
                target.getCard().hasType(CardType.BATTLE), target.getCounterCount(CounterType.DEFENSE));
    }

    /**
     * Marks each permanent this effect actually dealt damage to so it is exiled instead of dying this turn
     * ("a permanent dealt damage this way"); a target whose damage was prevented or was zero is not marked.
     */
    private void markDamagedPermanentsForExile(GameData gameData, StackEntry entry, Map<UUID, Integer> damageBefore) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanent.getId(), 0)
                        > damageBefore.getOrDefault(permanent.getId(), 0)) {
                    gameData.exileInsteadOfDying(entry, permanent.getId());
                }
            }
        }
    }

    /**
     * "Deals N damage to each [permanent of a set] [and each player]": the set is found once through
     * {@link SubjectResolutionService} as the effect is applied (CR 608.2h) and the amount evaluated once, against the
     * dealer as it is or last existed and the resolving entry's values ("that much"), or with
     * {@code amountForEachRecipient} for each permanent with it as the amount's source, every amount fixed before any
     * damage; each of its creatures, planeswalkers and battles (CR 120.1), then each player {@code players} names, is
     * dealt the damage in the effect's one damage event, every prevention (protection included) an option of its
     * damage pool (CR 616.1). With {@link DamageSource#TRIGGERING_PERMANENT} the permanent the trigger event was about
     * deals it ("it deals 4 damage to each creature blocking it", Ib Halfheart; "it deals that much damage to each
     * other creature and each player", Arcbond): the set is then found with that permanent as the source, and its
     * damage is this step's. The riders: a creature (a permanent) dealt damage this way is exiled instead if it would
     * die this turn, and a creature dealt damage this way can't be regenerated this turn (CR 701.19c).
     */
    private void damageEachPermanent(GameData gameData, StackEntry entry, DealDamageEffect e) {
        StackEntry dealing = e.damageSource() == DamageSource.TRIGGERING_PERMANENT
                ? damageSupport.entryDealtByTriggeringPermanent(gameData, entry) : entry;
        if (dealing == null) {
            return;
        }
        if (e.subject() instanceof PermanentSubject.ControlledBy set
                && subjectResolutionService.players(gameData, dealing, e, set.players()).isEmpty()) {
            return;
        }
        Permanent dealer = dealerOf(gameData, dealing);
        int damage = amountEvaluationService.evaluate(gameData, e.damage(), AmountContext.forStackEntry(entry, dealer));
        // Only a creature, planeswalker or battle can be dealt damage (CR 120.1): a set whose filter admits a land
        // ("each creature without flying" written without the creature filter) deals it none.
        List<Permanent> recipients = subjectResolutionService.permanents(gameData, dealing, e, e.subject()).stream()
                .filter(permanent -> damageSupport.isAnyTargetDamageRecipient(gameData, permanent))
                .toList();
        Map<UUID, Integer> amounts = new HashMap<>();
        for (Permanent permanent : recipients) {
            amounts.put(permanent.getId(), e.amountForEachRecipient()
                    ? amountEvaluationService.evaluate(gameData, e.damage(), AmountContext.forStackEntry(entry, permanent))
                    : damage);
        }
        boolean exileCreatures = e.exile() == ExileIfItWouldDie.DAMAGED_CREATURE
                || e.exile() == ExileIfItWouldDie.DAMAGED_PERMANENT;
        Map<UUID, Integer> damageBefore = e.exile() == ExileIfItWouldDie.DAMAGED_PERMANENT
                ? new HashMap<>(gameData.damageDealtToPermanentsThisTurn) : Map.of();
        boolean previousExilesCreaturesDamaged = dealing.isExilesCreaturesDamaged();
        dealing.setExilesCreaturesDamaged(previousExilesCreaturesDamaged || exileCreatures);
        try {
            damageSupport.damageFilteredCreatures(gameData, dealing, permanent -> amounts.get(permanent.getId()),
                    recipients, permanent -> true, exileCreatures, e.cantRegenerate());
            if (e.players() != null) {
                for (UUID playerId : subjectResolutionService.players(gameData, dealing, e, e.players())) {
                    damageSupport.dealDamageToPlayer(gameData, dealing, playerId, damage);
                }
            }
        } finally {
            dealing.setExilesCreaturesDamaged(previousExilesCreaturesDamaged);
        }
        if (e.exile() == ExileIfItWouldDie.DAMAGED_PERMANENT) {
            markDamagedPermanentsForExile(gameData, dealing, damageBefore);
        }
        if (dealing != entry) {
            entry.addToStep(StepKind.DEAL_DAMAGE, dealing.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * "Deals N damage to each of [up to] N targets": the amount is evaluated once and each of the effect's targets
     * still on the battlefield or in the game is dealt the full amount (CR 115.4), a player through the player
     * damage pool, a permanent through its own; one damage event.
     */
    private void damageEachTarget(GameData gameData, StackEntry entry, DealDamageEffect e) {
        List<UUID> targets = entry.isTargetIdsFromAssignments()
                ? entry.getTargetIds()
                : entry.targetsForEffect(e);
        if (targets.isEmpty()) {
            if (entry.getTargetId() == null) {
                return;
            }
            targets = List.of(entry.getTargetId());
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, dealerOf(gameData, entry)));
        for (UUID targetId : targets) {
            if (gameData.playerIds.contains(targetId)) {
                damageSupport.dealDamageToPlayer(gameData, entry, targetId, damage);
                continue;
            }
            Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
            if (permanent != null) {
                damageSupport.dealCreatureDamage(gameData, entry, permanent, damage);
            }
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    /** The entry's source permanent as it is, else as it last existed (an amount read from it, CR 608.2h). */
    private Permanent dealerOf(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source == null ? entry.getSourcePermanentSnapshot() : source;
    }

    /** "And X damage to itself": the live source permanent, through the permanent-damage path. */
    private void damageSource(GameData gameData, StackEntry entry, DealDamageEffect e) {
        Permanent source = subjectResolutionService.permanent(gameData, entry, e, e.subject());
        if (source == null) return;

        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        damageSupport.resolveAnyTargetDamage(gameData, entry, source.getId(), damage, false);
    }

    /**
     * "Deals damage to it": the permanent the trigger event was about, as it is when the ability resolves, if it is
     * still a creature, planeswalker or battle (CR 120.1); protection from the source and "prevent all damage this
     * would deal" apply as to a targeted creature.
     */
    private void damageTriggeringPermanent(GameData gameData, StackEntry entry, DealDamageEffect e) {
        Permanent triggering = subjectResolutionService.permanent(gameData, entry, e, e.subject());
        if (triggering == null || !gameQueryService.isCreature(gameData, triggering)
                && !gameQueryService.isPlaneswalker(gameData, triggering)
                && !triggering.getCard().hasType(CardType.BATTLE)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        if (damageSupport.isDamagePreventedForCreature(gameData, entry, triggering)) {
            return;
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" deals " + damage + " damage to ")
                .card(triggering.getCard()).text(".").build());
        damageSupport.dealCreatureDamage(gameData, entry, triggering, damage);
    }

    /**
     * "Deals 2 damage to [the creature chosen]": each permanent an earlier step of this resolution named, as it is now,
     * if it is still a creature; protection from the source and "prevent all damage this would deal" apply as to a
     * targeted creature.
     */
    private void damageNamedThisWay(GameData gameData, StackEntry entry, DealDamageEffect e) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        for (Permanent creature : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            if (!gameQueryService.isCreature(gameData, creature)
                    || damageSupport.isDamagePreventedForCreature(gameData, entry, creature)) {
                continue;
            }
            damageSupport.dealCreatureDamage(gameData, entry, creature, damage);
        }
    }

    /**
     * An Aura's damage to its creature: the Aura, as it is or last existed (CR 608.2h), must be attached and deals the
     * damage to the creature the subject names.
     */
    private void damageAuraHost(GameData gameData, StackEntry entry, DealDamageEffect e) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || !aura.isAttached()) {
            return;
        }
        Permanent creature = subjectResolutionService.permanent(gameData, entry, e, e.subject());
        if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, aura));
        damageSupport.dealCreatureDamage(gameData, entry, creature, damage, aura);
    }

    /**
     * Deals damage to the players the effect's {@link PlayerSubject} names, found through
     * {@link SubjectResolutionService} (several in APNAP order), one {@link DamageSupport#dealDamageToPlayer} call per
     * player, then checks the win condition once. The stack entry's controller id is never remapped, so
     * opponent-source damage-reduction shields keep applying (the source stays the caster). With
     * {@code DamageSource.EVENT_DAMAGE_SOURCE} the damage is dealt through
     * {@link DamageSupport#entryDealtByEventDamageSource}, whose source is the permanent that dealt the trigger event's
     * damage; with {@code DamageSource.TRIGGERING_PERMANENT} through {@link DamageSupport#entryDealtByTriggeringPermanent},
     * whose source is the permanent the trigger event was about; the players and the amount are still read from the
     * resolving entry.
     *
     * <p>The amount is evaluated for each player dealt damage, every player's before any damage is dealt (CR 608.2h):
     * with that player as the controller for {@code eachPlayer()} and with {@code amountForEachRecipient}, as the target
     * for {@code defendingPlayer()}, else in the resolving entry's own context. Every prevention is an option of each
     * player's damage pool, ordered by that player (CR 616.1).
     */
    private void damagePlayers(GameData gameData, StackEntry entry, DealDamageEffect e) {
        // The entry the damage is dealt through: the resolving one, or one whose source is the permanent that dealt
        // the trigger event's damage ("that Archer deals that much damage"); the players are always read from the
        // resolving entry.
        StackEntry dealing = switch (e.damageSource()) {
            case EVENT_DAMAGE_SOURCE -> damageSupport.entryDealtByEventDamageSource(gameData, entry);
            case TRIGGERING_PERMANENT -> damageSupport.entryDealtByTriggeringPermanent(gameData, entry);
            case ABILITY_SOURCE -> entry;
        };
        if (dealing == null) {
            return;
        }
        UUID damageSourceId = dealing.getSourcePermanentId() != null
                ? dealing.getSourcePermanentId() : dealing.getCard().getId();
        int damageBefore = gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0);
        boolean previousUnpreventable = gameData.unpreventableDamageInProgress;
        gameData.unpreventableDamageInProgress = previousUnpreventable || e.alwaysUnpreventable();
        try {
            if (e.recipient() == Recipient.PLAYER_OR_THEIR_PLANESWALKER) {
                damagePlayerOrTheirPlaneswalker(gameData, entry, dealing, e);
            } else {
                damageEachPlayer(gameData, entry, dealing, e);
            }
            if (dealing != entry) {
                // Dealt through the permanent that deals it: the damage is this step's.
                entry.addToStep(StepKind.DEAL_DAMAGE, dealing.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
            }

            if (e.recordDamageDealt()) {
                entry.setEventValue(gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0)
                        - damageBefore);
            }
            gameOutcomeService.checkWinCondition(gameData);
        } finally {
            gameData.unpreventableDamageInProgress = previousUnpreventable;
        }
    }

    private void damageEachPlayer(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageEffect e) {
        List<UUID> players = subjectResolutionService.players(gameData, entry, e, e.players());
        if (players.isEmpty()) {
            return;
        }
        boolean spellController = readsTarget(e, PermanentSubject.TargetSpell.class);
        // Every amount is determined once, before any of the damage changes the game (CR 608.2h).
        Map<UUID, Integer> amounts = new LinkedHashMap<>();
        for (UUID playerId : players) {
            amounts.put(playerId, evaluatePlayerAmount(gameData, entry, e, playerId));
        }
        amounts.forEach((playerId, amount) -> {
            if (!(spellController && amount <= 0)) {
                damageSupport.dealDamageToPlayer(gameData, dealing, playerId, amount);
            }
        });
    }

    private static boolean readsTarget(DealDamageEffect e, Class<? extends PermanentSubject> kind) {
        return e.players() instanceof PlayerSubject.ControllerOf controllerOf
                && kind.isInstance(controllerOf.permanent());
    }

    /**
     * "That player or a planeswalker that player controls": the player is found once, then, if they control a
     * planeswalker, the controller chooses without targeting and the effect is resolved again with the answer; with
     * none, the damage goes to that player, whatever the entry's own target is (a spell-cast trigger's "that player").
     */
    private void damagePlayerOrTheirPlaneswalker(GameData gameData, StackEntry entry, StackEntry dealing,
                                                 DealDamageEffect effect) {
        UUID player = entry.getOpponentChosenTargetPlayerId();
        UUID recipient;
        if (player == null) {
            player = subjectResolutionService.player(gameData, entry, effect, effect.players());
            if (player == null || !gameData.playerIds.contains(player)) return;
            entry.setOpponentChosenTargetPlayerId(player);
            UUID thatPlayer = player;
            List<UUID> planeswalkers = gameData.playerBattlefields.getOrDefault(thatPlayer, List.of()).stream()
                    .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                    .map(Permanent::getId).toList();
            if (!planeswalkers.isEmpty()) {
                entry.setTargetId(null);
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.RandomOpponentDamageChoice(entry.getCard(), entry.getControllerId()));
                playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), planeswalkers,
                        List.of(thatPlayer), FaceDownVisibility.sourceName(gameData, entry)
                                + " \u2014 Choose " + gameData.playerIdToName.get(thatPlayer)
                                + " or a planeswalker they control.");
                return;
            }
            recipient = player;
        } else {
            recipient = entry.getTargetId() == null ? player : entry.getTargetId();
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        int amount = evaluatePlayerAmount(gameData, entry, effect, player);
        damageSupport.resolveAnyTargetDamage(gameData, dealing, recipient, amount, false);
    }

    private int evaluatePlayerAmount(GameData gameData, StackEntry entry, DealDamageEffect e, UUID playerId) {
        // Source-relative amounts use the live source permanent when it is still on the battlefield,
        // else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        if (e.amountForEachRecipient()
                || e.players() instanceof PlayerSubject.Each each && each.relation() == PlayerSubject.Relation.ALL) {
            context = context.withControllerId(playerId);
        } else if (e.players() instanceof PlayerSubject.DefendingPlayer) {
            context = context.withTargetPermanentId(playerId);
        }
        return amountEvaluationService.evaluate(gameData, e.damage(), context);
    }
}
