package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Sealed hierarchy of trigger event contexts.
 * Each record carries the event-specific data that trigger collectors may need.
 */
public sealed interface TriggerContext {

    record SpellCopy(StackEntry copiedSpell, UUID copyingPlayerId) implements TriggerContext {
        public Card spellCard() {
            return copiedSpell.getCard();
        }
    }

    default boolean causedByCreatureDying() {
        return false;
    }

    /**
     * Context for spell-cast triggers (ON_ANY_PLAYER_CASTS_SPELL, ON_CONTROLLER_CASTS_SPELL, ON_OPPONENT_CASTS_SPELL).
     */
    record SpellCast(Card spellCard, UUID castingPlayerId, Zone castZone,
                     UUID exiledSourcePermanentId) implements TriggerContext {

        public SpellCast(Card spellCard, UUID castingPlayerId, Zone castZone) {
            this(spellCard, castingPlayerId, castZone, null);
        }

        /**
         * Legacy hand/not-hand form. {@code false} maps to {@link Zone#GRAVEYARD}, matching what the
         * non-hand cast sites (graveyard, exile, free-cast) meant before zones were carried; the
         * library-top cast path passes {@link Zone#LIBRARY} explicitly instead.
         */
        public SpellCast(Card spellCard, UUID castingPlayerId, boolean castFromHand) {
            this(spellCard, castingPlayerId, castFromHand ? Zone.HAND : Zone.GRAVEYARD);
        }

        public boolean castFromHand() {
            return castZone == Zone.HAND;
        }
    }

    /**
     * A permanent, or a player when {@code targetPermanent} is {@code null}, became the target of a spell or ability:
     * "if a creature you control becoming the target of a spell or ability causes a triggered ability of a permanent
     * you control to trigger" (Valiant Emberkin) reads the permanent and its controller.
     */
    record BecomesTarget(Permanent targetPermanent, UUID targetControllerId) implements TriggerContext {}

    /** Context for opponent-mill triggers. */
    record Mill(UUID milledPlayerId, int milledCount) implements TriggerContext {}

    /** Context for a player drawing a card, which the "triggers an additional time" effects of draws read. */
    record Draw(UUID drawingPlayerId) implements TriggerContext {}

    /**
     * Context for damage-dealt-to-controller triggers (ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).
     */
    record DamageToController(UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) implements TriggerContext {}

    /**
     * Context for damage-dealt-to-controller triggers that only care about the amount
     * (ON_CONTROLLER_DEALT_DAMAGE, e.g. Living Artifact). Fired once per damage source.
     * {@code sourcePermanentId} is populated for source-specific opponent-damage triggers.
     */
    record DamageToControllerAmount(UUID damagedPlayerId, int amount, UUID sourcePermanentId,
            UUID sourceControllerId) implements TriggerContext {
        public DamageToControllerAmount(UUID damagedPlayerId, int amount, UUID sourcePermanentId) {
            this(damagedPlayerId, amount, sourcePermanentId, null);
        }

        public DamageToControllerAmount(UUID damagedPlayerId, int amount) {
            this(damagedPlayerId, amount, null, null);
        }
    }

    /** Context for batched ally-creature damage-to-player triggers. */
    record AllyCreaturesDealDamageToPlayer(UUID damagedPlayerId, UUID sourceControllerId,
                                            List<Permanent> damageDealers) implements TriggerContext {
        public AllyCreaturesDealDamageToPlayer {
            damageDealers = List.copyOf(damageDealers);
        }
    }

    /**
     * Context for dealt-damage-to-creature triggers (ON_DEALT_DAMAGE): the permanent dealt damage, the damage, the
     * source's controller as it dealt it, the source (its card, and its permanent if it was one) and
     * {@code damagedLastKnown}, the damaged permanent as it was dealt the damage with the damage keywords it had, which
     * the combat damage step takes before its creatures die; {@code null} has the collector take it from the
     * permanent, which is still on the battlefield.
     */
    record DamageToCreature(Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId,
                            Card sourceCard, UUID sourcePermanentId, Permanent damagedLastKnown)
            implements TriggerContext {
        public DamageToCreature(Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId,
                                Card sourceCard, UUID sourcePermanentId) {
            this(damagedCreature, damageDealt, damageSourceControllerId, sourceCard, sourcePermanentId, null);
        }

        public DamageToCreature(Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId) {
            this(damagedCreature, damageDealt, damageSourceControllerId, null, null);
        }
    }

    record OpponentPermanentDealtExcessDamage(Permanent damagedPermanent,
                                              UUID damagedPermanentControllerId,
                                              int excessDamage) implements TriggerContext {}

    /** Context for a creature dealing damage to another creature. */
    record CreatureDealsDamageToCreature(Permanent damageSource, UUID damagedCreatureId,
                                          int damageDealt, boolean combatDamage,
                                          Permanent damagedCreature, UUID damagedCreatureControllerId)
            implements TriggerContext {
        public CreatureDealsDamageToCreature(Permanent damageSource, UUID damagedCreatureId,
                                              int damageDealt, boolean combatDamage) {
            this(damageSource, damagedCreatureId, damageDealt, combatDamage, null, null);
        }
    }

    /** Context for a source dealing noncombat damage to a creature. */
    record SourceDealsNoncombatDamageToCreature(Permanent damagedCreature, int damageDealt,
                                                 UUID sourceControllerId) implements TriggerContext {}

    /** Context for global creature-damage triggers (ON_ANY_CREATURE_DEALT_DAMAGE). */
    record AnyCreatureDealtDamage(Permanent damagedCreature, UUID damagedCreatureControllerId,
                                  int damageDealt) implements TriggerContext {}

    /** Context for global permanent-damage triggers (ON_ANY_PERMANENT_DEALT_DAMAGE). */
    record AnyPermanentDealtDamage(Permanent damagedPermanent, UUID damagedPermanentControllerId,
                                   int damageDealt) implements TriggerContext {}

    /**
     * Context for noncombat-damage-to-opponent triggers (ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE).
     */
    record NoncombatDamageToOpponent(UUID damagedPlayerId, UUID sourceControllerId, int damageAmount)
            implements TriggerContext {

        public NoncombatDamageToOpponent(UUID damagedPlayerId) {
            this(damagedPlayerId, null, 0);
        }
    }

    /**
     * Context for enter-the-battlefield triggers (ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
     * ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
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
     * @param enteringPermanentId   the permanent that entered, when the scan knows it ({@code null} otherwise)
     */
    record PermanentEnters(Card enteringCard, UUID enteringControllerId, UUID defaultTargetPlayerId,
                           int perEffectTriggerCount, UUID enteringPermanentId) implements TriggerContext {}

    /** Context for triggers watching a permanent being turned face up. */
    record PermanentTurnsFaceUp(Permanent turnedPermanent, UUID controllerId) implements TriggerContext {}

    // ── Death / leaves-battlefield contexts ────────────────────────────

    /**
     * Context for creature-death triggers that reference the dying creature's card and controller.
     * Shared by Triggers.creatureDies(YOU_CONTROL), Triggers.creatureDies(ANY), Triggers.nontokenCreatureDies(YOU_CONTROL),
     * Triggers.nontokenCreatureDies(ANY), Triggers.nontokenCreatureDies(ENCHANTED_PLAYER_CONTROLS), and
     * Triggers.creatureDies(OPPONENT_CONTROLS). {@code dyingCreaturePower} is the
     * dying creature's last-known effective power on the battlefield (Kresh the Bloodbraided) and
     * {@code dyingCreatureToughness} its last-known effective toughness (Grim Feast).
     * {@code dyingPermanent} preserves the dying permanent's counter state for trigger collectors
     * that need last-known counters, and {@code attachments} the Auras and Equipment attached to it just before it
     * died (CR 603.10a), for "for each Aura you controlled that was attached to it".
     */
    record CreatureDeath(Card dyingCard, UUID dyingCreatureControllerId, int dyingCreaturePower,
                         int dyingCreatureToughness, UUID dyingPermanentId,
                         Permanent dyingPermanent, boolean wasCreature,
                         com.github.laxika.magicalvibes.model.rules.DepartedAttachments attachments)
            implements TriggerContext {

        public CreatureDeath {
            attachments = attachments == null
                    ? com.github.laxika.magicalvibes.model.rules.DepartedAttachments.NONE : attachments;
        }

        public CreatureDeath(Card dyingCard, UUID dyingCreatureControllerId, int dyingCreaturePower,
                             int dyingCreatureToughness, UUID dyingPermanentId,
                             Permanent dyingPermanent, boolean wasCreature) {
            this(dyingCard, dyingCreatureControllerId, dyingCreaturePower, dyingCreatureToughness,
                    dyingPermanentId, dyingPermanent, wasCreature, null);
        }

        public CreatureDeath(Card dyingCard, UUID dyingCreatureControllerId, int dyingCreaturePower,
                             int dyingCreatureToughness) {
            this(dyingCard, dyingCreatureControllerId, dyingCreaturePower, dyingCreatureToughness,
                    null, null, true);
        }

        public CreatureDeath(Card dyingCard, UUID dyingCreatureControllerId, int dyingCreaturePower,
                             int dyingCreatureToughness, UUID dyingPermanentId) {
            this(dyingCard, dyingCreatureControllerId, dyingCreaturePower, dyingCreatureToughness,
                    dyingPermanentId, null, true);
        }

        public CreatureDeath(Card dyingCard, UUID dyingCreatureControllerId, int dyingCreaturePower,
                             int dyingCreatureToughness, UUID dyingPermanentId,
                             Permanent dyingPermanent) {
            this(dyingCard, dyingCreatureControllerId, dyingCreaturePower, dyingCreatureToughness,
                    dyingPermanentId, dyingPermanent, true);
        }

        @Override
        public boolean causedByCreatureDying() {
            return wasCreature;
        }
    }

    /**
     * Context for ON_ANY_SOURCE_DEALS_DAMAGE triggers. Carries the damage source object, its
     * controller, the source permanent when applicable, and the total damage the source dealt in
     * this event (already summed across every simultaneous target).
     */
    record SourceDealsDamage(Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId,
                             int totalDamage, Map<UUID, Integer> damageToPlayers,
                             UUID singleCreatureSpellTargetId,
                             Map<UUID, Integer> damageToPermanents,
                             boolean combatDamage, Map<UUID, Permanent> damagedPermanentSnapshots) implements TriggerContext {
        public SourceDealsDamage {
            damagedPermanentSnapshots = Map.copyOf(damagedPermanentSnapshots);
        }

        public SourceDealsDamage(Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId,
                                 int totalDamage, Map<UUID, Integer> damageToPlayers,
                                 UUID singleCreatureSpellTargetId, Map<UUID, Integer> damageToPermanents,
                                 boolean combatDamage) {
            this(sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers,
                    singleCreatureSpellTargetId, damageToPermanents, combatDamage, Map.of());
        }
        public SourceDealsDamage(Card sourceCard, UUID sourceControllerId, int totalDamage) {
            this(sourceCard, sourceControllerId, null, totalDamage, Map.of(), null, Map.of(), false);
        }

        public SourceDealsDamage(Card sourceCard, UUID sourceControllerId, int totalDamage,
                                 Map<UUID, Integer> damageToPlayers) {
            this(sourceCard, sourceControllerId, null, totalDamage, damageToPlayers, null, Map.of(), false);
        }

        public SourceDealsDamage(Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId,
                                 int totalDamage, Map<UUID, Integer> damageToPlayers) {
            this(sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers, null, Map.of(), false);
        }
    }

    /** Context for a source's combat-damage-only self trigger. */
    record SourceDealsCombatDamage(Card sourceCard, UUID sourceControllerId,
                                   UUID sourcePermanentId, int totalDamage,
                                   int damageToPlayers, UUID damagedPlayerId) implements TriggerContext {
        public SourceDealsCombatDamage(Card sourceCard, UUID sourceControllerId,
                                       UUID sourcePermanentId, int totalDamage) {
            this(sourceCard, sourceControllerId, sourcePermanentId, totalDamage, totalDamage, null);
        }

        public SourceDealsCombatDamage(Card sourceCard, UUID sourceControllerId,
                                       UUID sourcePermanentId, int totalDamage, int damageToPlayers) {
            this(sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers, null);
        }
    }

    record CreatureDealsDamageToPlaneswalker(Permanent damageSource, UUID damagedPlaneswalkerId,
                                              int damage, boolean combatDamage)
            implements TriggerContext {
    }

    /**
     * Context for ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT triggers (Mangara's Equity).
     *
     * @param damageSource     the creature that dealt the damage
     * @param damagedPlayerId  the watcher's controller — the damaged player, or the damaged
     *                         permanent's controller
     * @param damagedPermanent the permanent that was damaged, or {@code null} when the player was
     * @param damage           how much damage that creature dealt in this event
     */
    record CreatureDamageToYouOrYourPermanent(Permanent damageSource, UUID damagedPlayerId,
                                              Permanent damagedPermanent, int damage) implements TriggerContext {}

    /** Context for any opponent-controlled source damaging this permanent's controller or their permanent. */
    record SourceDamageToYouOrYourPermanent(Card sourceCard, UUID sourceControllerId,
                                            UUID sourcePermanentId, UUID damagedPlayerId,
                                            UUID damagedPermanentId) implements TriggerContext {
        SourceDamageToYouOrYourPermanent(Card sourceCard, UUID sourceControllerId,
                                         UUID sourcePermanentId, UUID damagedPlayerId) {
            this(sourceCard, sourceControllerId, sourcePermanentId, damagedPlayerId, null);
        }
    }
}
