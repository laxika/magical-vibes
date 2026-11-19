package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.Objects;

/**
 * Deals damage to one or more players (never creatures). The {@link PlayerSubject} names who is dealt the damage
 * ({@code SubjectResolutionService} resolves it); the {@link DynamicAmount} is evaluated at resolution (fixed number,
 * cards in a hand, +1/+1 counter count, …). For {@link Subjects#eachPlayer()} the amount is evaluated once per player,
 * with that player as the controller, so player-relative scopes such as {@code CONTROLLER} refer to the player
 * receiving the damage; for {@link Subjects#defendingPlayer()} with the defending player as the target.
 *
 * <p>{@code attachedCountFilter} is non-null only for the Curse of Thirst case: the amount dealt is the number of
 * permanents attached to the player dealt the damage that match the predicate, and {@code amount} is ignored.
 *
 * <p>Only a declared {@link Subjects#targetPlayer()} declares a player target. "That creature's controller"
 * ({@code controllerOf(target())}) and "that spell's controller" ({@code controllerOf(targetSpell())}) declare the
 * permanent or spell target they read, so a card whose only targeting effect is this one still has its target.
 *
 * @param unpreventable              whether the damage can't be prevented
 * @param damageSource               which object deals the damage: the ability's source, the permanent that dealt the
 *                                   damage the trigger event was about ({@link #dealtByEventDamageSource()}), or the
 *                                   permanent the trigger event was about ({@link #dealtByTriggeringPermanent()});
 *                                   never {@code null}
 * @param playerOrTheirPlaneswalker  whether the damage goes to the player {@code player} names or, chosen without
 *                                   targeting as it resolves, a planeswalker that player controls ("deals 1 damage to
 *                                   that player or a planeswalker that player controls", Curse of the Pierced Heart)
 */
public record DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player,
                                        PermanentPredicate attachedCountFilter, boolean unpreventable,
                                        boolean recordDamageDealt, TargetFilter triggeredTargetFilter,
                                        DamageSource damageSource, boolean playerOrTheirPlaneswalker)
        implements DamageDealingEffect {

    public DealDamageToPlayersEffect {
        Objects.requireNonNull(player, "player");
        if (damageSource == null) {
            damageSource = DamageSource.ABILITY_SOURCE;
        }
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player,
                                     PermanentPredicate attachedCountFilter, boolean unpreventable,
                                     boolean recordDamageDealt, TargetFilter triggeredTargetFilter,
                                     DamageSource damageSource) {
        this(amount, player, attachedCountFilter, unpreventable, recordDamageDealt, triggeredTargetFilter,
                damageSource, false);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player,
                                     PermanentPredicate attachedCountFilter, boolean unpreventable,
                                     boolean recordDamageDealt, TargetFilter triggeredTargetFilter) {
        this(amount, player, attachedCountFilter, unpreventable, recordDamageDealt, triggeredTargetFilter,
                DamageSource.ABILITY_SOURCE);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player,
                                     PermanentPredicate attachedCountFilter, boolean unpreventable,
                                     boolean recordDamageDealt) {
        this(amount, player, attachedCountFilter, unpreventable, recordDamageDealt, null);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player,
                                     PermanentPredicate attachedCountFilter, boolean unpreventable) {
        this(amount, player, attachedCountFilter, unpreventable, false);
    }

    /** Records actual damage in the entry's event value for a subsequent effect. */
    public DealDamageToPlayersEffect recordingDamageDealt() {
        return new DealDamageToPlayersEffect(amount, player, attachedCountFilter, unpreventable, true,
                triggeredTargetFilter, damageSource, playerOrTheirPlaneswalker);
    }

    /** Supplies a target filter for trigger-time target selection when the effect is granted. */
    public DealDamageToPlayersEffect withTriggeredTargetFilter(TargetFilter targetFilter) {
        return new DealDamageToPlayersEffect(amount, player, attachedCountFilter, unpreventable,
                recordDamageDealt, targetFilter, damageSource, playerOrTheirPlaneswalker);
    }

    /**
     * The permanent that dealt the damage the trigger event was about deals this damage instead of the ability's
     * source ({@link DamageSource#EVENT_DAMAGE_SOURCE}): "that Archer deals that much damage to that creature's
     * controller" (Greatbow Doyen), "it deals that much damage to the chosen player" (Saskia the Unyielding).
     */
    public DealDamageToPlayersEffect dealtByEventDamageSource() {
        return new DealDamageToPlayersEffect(amount, player, attachedCountFilter, unpreventable,
                recordDamageDealt, triggeredTargetFilter, DamageSource.EVENT_DAMAGE_SOURCE, playerOrTheirPlaneswalker);
    }

    /**
     * The permanent the trigger event was about deals this damage instead of the ability's source
     * ({@link DamageSource#TRIGGERING_PERMANENT}): "then that land deals 1 damage to that player" (Barbflare Gremlin).
     */
    public DealDamageToPlayersEffect dealtByTriggeringPermanent() {
        return new DealDamageToPlayersEffect(amount, player, attachedCountFilter, unpreventable,
                recordDamageDealt, triggeredTargetFilter, DamageSource.TRIGGERING_PERMANENT, playerOrTheirPlaneswalker);
    }

    public DealDamageToPlayersEffect(int damage, PlayerSubject player) {
        this(new Fixed(damage), player, null, false);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player) {
        this(amount, player, null, false);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player,
                                     PermanentPredicate attachedCountFilter) {
        this(amount, player, attachedCountFilter, false);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, PlayerSubject player, boolean unpreventable) {
        this(amount, player, null, unpreventable);
    }

    /**
     * Deals the damage to each opponent who said yes to the preceding "each player may" choice of this resolution
     * (the stack entry's accepted players, {@link Subjects#acceptedPlayers()}): Snort's "each opponent who discarded
     * their hand this way".
     */
    public static DealDamageToPlayersEffect selectedOpponents(DynamicAmount amount) {
        return new DealDamageToPlayersEffect(amount, Subjects.except(Subjects.acceptedPlayers(), Subjects.you()),
                null, false);
    }

    public static DealDamageToPlayersEffect selectedOpponents(int damage) {
        return selectedOpponents(new Fixed(damage));
    }

    /**
     * Deals damage to "that player" (the enchanted player whose upkeep it is) equal to the number of permanents
     * attached to that player matching the predicate (e.g. {@code PermanentHasSubtypePredicate(CardSubtype.CURSE)}
     * for Curse of Thirst).
     */
    public static DealDamageToPlayersEffect enchantedAttachedCount(PermanentPredicate predicate) {
        return new DealDamageToPlayersEffect(new Fixed(0), Subjects.triggeringPlayer(), predicate);
    }

    /**
     * Deals {@code damage} to the player {@code player} names or to a planeswalker that player controls, chosen as it
     * resolves without targeting ("deals 1 damage to that player or a planeswalker that player controls").
     */
    public static DealDamageToPlayersEffect toPlayerOrTheirPlaneswalker(int damage, PlayerSubject player) {
        return new DealDamageToPlayersEffect(new Fixed(damage), player, null, false, false, null,
                DamageSource.ABILITY_SOURCE, true);
    }

    /**
     * A declared {@code targetPlayer()} targets a player. "That creature's controller" ({@code controllerOf(target())})
     * and "that spell's controller" ({@code controllerOf(targetSpell())}) declare the permanent or spell they read, so
     * the target is chosen even when no companion effect declares it (Fodder Launch, Ionize). Benign: the damage lands
     * on a player, not the permanent or spell; the kept {@code @ValidatesTarget} validator enforces "must be a player"
     * for the player target, which the no-op player spec cannot express.
     */
    @Override
    public TargetSpec targetSpec() {
        if (player.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (player instanceof PlayerSubject.ControllerOf controllerOf) {
            if (controllerOf.permanent() instanceof PermanentSubject.Target) {
                return TargetSpec.benign(TargetPredicates.permanent());
            }
            if (controllerOf.permanent() instanceof PermanentSubject.TargetSpell) {
                return TargetSpec.benign(TargetPredicates.spellOnStack());
            }
        }
        return TargetSpec.NONE;
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean damagesController() {
        return player instanceof PlayerSubject.You;
    }
}
