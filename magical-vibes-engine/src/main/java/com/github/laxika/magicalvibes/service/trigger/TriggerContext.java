package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.UUID;

/**
 * Sealed hierarchy of trigger event contexts.
 * Each record carries the event-specific data that trigger collectors may need.
 */
public sealed interface TriggerContext {

    /**
     * Context for spell-cast triggers (ON_ANY_PLAYER_CASTS_SPELL, ON_CONTROLLER_CASTS_SPELL, ON_OPPONENT_CASTS_SPELL).
     */
    record SpellCast(Card spellCard, UUID castingPlayerId, boolean castFromHand) implements TriggerContext {}

    /**
     * Context for discard triggers (ON_OPPONENT_DISCARDS).
     */
    record Discard(UUID discardingPlayerId, Card discardedCard) implements TriggerContext {}

    /**
     * Context for land-tap triggers (ON_ANY_PLAYER_TAPS_LAND).
     */
    record LandTap(UUID tappingPlayerId, UUID tappedLandId) implements TriggerContext {}

    /**
     * Context for damage-dealt-to-controller triggers (ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).
     */
    record DamageToController(UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) implements TriggerContext {}

    /**
     * Context for damage-dealt-to-controller triggers that only care about the amount
     * (ON_CONTROLLER_DEALT_DAMAGE, e.g. Living Artifact). Fired once per damage source.
     */
    record DamageToControllerAmount(UUID damagedPlayerId, int amount) implements TriggerContext {}

    /**
     * Context for ally-permanent-sacrificed triggers (ON_ALLY_PERMANENT_SACRIFICED).
     */
    record AllySacrificed(UUID sacrificingPlayerId, Card sacrificedCard) implements TriggerContext {}

    /**
     * Context for dealt-damage-to-creature triggers (ON_DEALT_DAMAGE).
     */
    record DamageToCreature(Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId) implements TriggerContext {}

    /**
     * Context for enchanted-permanent-tap triggers (ON_ENCHANTED_PERMANENT_TAPPED).
     */
    record EnchantedPermanentTap(Permanent tappedPermanent, UUID tappedPermanentControllerId) implements TriggerContext {}

    /**
     * Context for life-loss triggers (ON_OPPONENT_LOSES_LIFE).
     */
    record LifeLoss(UUID losingPlayerId, int lifeLostAmount) implements TriggerContext {}

    /**
     * Context for life-gain triggers (ON_CONTROLLER_GAINS_LIFE).
     * {@code sourceCard} and {@code sourceEntryType} identify what caused the life gain
     * (e.g. a spell with lifelink). Both may be null for non-spell sources.
     */
    record LifeGain(UUID gainingPlayerId, int lifeGainedAmount, Card sourceCard, StackEntryType sourceEntryType) implements TriggerContext {

        /** Backward-compatible constructor for life gain with no source info. */
        public LifeGain(UUID gainingPlayerId, int lifeGainedAmount) {
            this(gainingPlayerId, lifeGainedAmount, null, null);
        }
    }

    /**
     * Context for noncombat-damage-to-opponent triggers (ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE).
     */
    record NoncombatDamageToOpponent(UUID damagedPlayerId) implements TriggerContext {}

    /**
     * Context for creature-card-milled triggers (ON_OPPONENT_CREATURE_CARD_MILLED).
     */
    record CreatureCardMilled(UUID milledPlayerId, Card milledCard) implements TriggerContext {}

    /**
     * Context for enter-the-battlefield triggers (ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
     * ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
     * ON_OPPONENT_LAND_ENTERS_BATTLEFIELD, ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD).
     *
     * @param enteringCard          the permanent that entered and caused the scan
     * @param enteringControllerId  the controller of the permanent that entered
     * @param defaultTargetPlayerId the player recorded as the queued ability's target
     *                              ({@code null} for scans that leave the target unset, e.g. the
     *                              ally/any-creature scans)
     * @param perEffectTriggerCount how many copies of each triggered ability to enqueue
     *                              (e.g. Naban doubling on the any-creature scan)
     * @param mayPayTargetCardId    the card id preserved on a {@code MayPayManaEffect} stack entry
     *                              so the wrapped effect can reference the entering permanent
     *                              (e.g. Mirrorworks); {@code null} when unused
     */
    record PermanentEnters(Card enteringCard, UUID enteringControllerId, UUID defaultTargetPlayerId,
                           int perEffectTriggerCount, UUID mayPayTargetCardId) implements TriggerContext {}

    // ── Death / leaves-battlefield contexts ────────────────────────────

    /**
     * Context for a card's own death triggers (ON_DEATH).
     * The dying permanent may be null when the 4-arg overload is used.
     */
    record SelfDeath(Card dyingCard, UUID controllerId, boolean wasCreature,
                     Permanent dyingPermanent) implements TriggerContext {}

    /**
     * Context for creature-death triggers that reference the dying creature's card and controller.
     * Shared by ON_ALLY_CREATURE_DIES, ON_ANY_CREATURE_DIES, ON_ALLY_NONTOKEN_CREATURE_DIES,
     * ON_ANY_NONTOKEN_CREATURE_DIES, and ON_OPPONENT_CREATURE_DIES. {@code dyingCreaturePower} is the
     * dying creature's last-known effective power on the battlefield (Kresh the Bloodbraided).
     */
    record CreatureDeath(Card dyingCard, UUID dyingCreatureControllerId, int dyingCreaturePower) implements TriggerContext {}

    /**
     * Context for ON_EQUIPPED_CREATURE_DIES triggers.
     */
    record EquippedCreatureDeath(UUID dyingCreatureId,
                                 UUID dyingCreatureControllerId) implements TriggerContext {}

    /**
     * Context for ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD triggers.
     */
    record EnchantedPermanentDeath(UUID dyingPermanentId, UUID dyingPermanentControllerId,
                                   UUID dyingCreatureCardId, int dyingCreatureToughness) implements TriggerContext {}

    /**
     * Context for ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD triggers.
     *
     * @param leavingPermanent   the permanent that left the battlefield
     * @param leavingControllerId the player who controlled it as it left (last-known information)
     */
    record EnchantedPermanentLeaves(Permanent leavingPermanent, UUID leavingControllerId) implements TriggerContext {}

    /**
     * Context for ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD and
     * ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD triggers.
     */
    record ArtifactGraveyard(UUID graveyardOwnerId,
                             UUID artifactControllerId) implements TriggerContext {}

    /**
     * Context for ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Dingus Egg).
     */
    record AnyLandGraveyard(UUID graveyardOwnerId,
                            UUID landControllerId) implements TriggerContext {}

    /**
     * Context for ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE triggers (Compost).
     */
    record BlackCardOpponentGraveyard(UUID graveyardOwnerId,
                                      Card card) implements TriggerContext {}

    /**
     * Context for ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Prince of
     * Thralls).
     *
     * @param dyingCard          the permanent's card, now in {@code graveyardOwnerId}'s graveyard
     * @param dyingControllerId  the player who controlled the permanent on the battlefield ("that opponent")
     * @param graveyardOwnerId   the owner of the graveyard the card was put into
     */
    record OpponentPermanentGraveyard(Card dyingCard, UUID dyingControllerId,
                                      UUID graveyardOwnerId) implements TriggerContext {}

    /**
     * Context for ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT triggers (Sacred Ground).
     *
     * @param landCard          the land card that was put into the graveyard from the battlefield
     * @param graveyardOwnerId  the owner of the graveyard the land was put into
     * @param causeControllerId the controller of the spell or ability that caused it (an opponent)
     */
    record LandPutIntoGraveyard(Card landCard, UUID graveyardOwnerId,
                                UUID causeControllerId) implements TriggerContext {}

    /**
     * Context for ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE triggers (Soulcipher Board).
     *
     * @param creatureCard      the creature card that was put into the graveyard from anywhere
     * @param graveyardOwnerId  the owner of the graveyard the card was put into
     */
    record CreatureCardPutIntoGraveyard(Card creatureCard, UUID graveyardOwnerId) implements TriggerContext {}

    /**
     * Context for ON_SELF_LEAVES_BATTLEFIELD triggers.
     */
    record SelfLeaves(UUID controllerId) implements TriggerContext {}

    /**
     * Context for ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers.
     */
    record AllyAuraOrEquipmentGraveyard(Card dyingCard,
                                        UUID controllerId) implements TriggerContext {}

    /**
     * Context for ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD triggers.
     */
    record ControllerCardsLeaveGraveyard(UUID graveyardOwnerId) implements TriggerContext {}

    /**
     * Context for ON_ANY_SOURCE_DEALS_DAMAGE triggers (Justice). Carries the damage source object,
     * its controller (the reflection recipient), and the total damage the source dealt in this
     * event (already summed across every simultaneous target).
     */
    record SourceDealsDamage(Card sourceCard, UUID sourceControllerId, int totalDamage) implements TriggerContext {}
}
