package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;
import java.util.Map;
import java.util.List;

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
    record SpellCast(Card spellCard, UUID castingPlayerId, Zone castZone) implements TriggerContext {

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

    /** Context for "whenever a spell or ability you control counters a spell" triggers. */
    record SpellCountered(UUID counteringPlayerId) implements TriggerContext {}

    record Foretell(UUID foretellingPlayerId, Card foretoldCard) implements TriggerContext {}

    /** Context for "whenever a spell you've cast is countered" triggers. */
    record SpellCastCountered(UUID spellControllerId) implements TriggerContext {}

    /**
     * Context for land-play triggers (ON_CONTROLLER_PLAYS_LAND). Fired only when a land is actually
     * <em>played</em>, unlike the landfall path which also sees lands put onto the battlefield.
     */
    record LandPlayed(UUID playingPlayerId, Card landCard) implements TriggerContext {}

    /**
     * Context for discard triggers (ON_OPPONENT_DISCARDS).
     */
    record Discard(UUID discardingPlayerId, Card discardedCard) implements TriggerContext {}

    /** Context for a discard event containing one or more cards. */
    record DiscardEvent(UUID discardingPlayerId, int discardedCount) implements TriggerContext {}

    /** Context for controller-scry triggers. */
    record Scry(UUID scryingPlayerId, int bottomedCardCount) implements TriggerContext {
        public Scry(UUID scryingPlayerId) {
            this(scryingPlayerId, 0);
        }
    }

    /** Context for controller-investigate triggers. */
    record Investigate(UUID investigatingPlayerId) implements TriggerContext {}

    /** Context for controller-surveil triggers. */
    record Surveil(UUID surveilingPlayerId) implements TriggerContext {}

    /** Context for controller collect-evidence triggers. */
    record CollectEvidence(UUID collectingPlayerId) implements TriggerContext {}

    /**
     * Context for land-tap triggers (ON_ANY_PLAYER_TAPS_LAND).
     */
    record LandTap(UUID tappingPlayerId, UUID tappedLandId) implements TriggerContext {}

    /**
     * Context for controller-scoped creature-mana triggers
     * (ON_CONTROLLER_TAPS_CREATURE_FOR_MANA).
     */
    record CreatureTapForMana(UUID tappingPlayerId, UUID tappedCreatureId) implements TriggerContext {}

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

    /**
     * Context for ally-permanent-sacrificed triggers (ON_ALLY_PERMANENT_SACRIFICED).
     */
    record AllySacrificed(UUID sacrificingPlayerId, Card sacrificedCard) implements TriggerContext {}

    record OpponentNontokenPermanentSacrificed(UUID sacrificingPlayerId,
                                               Card sacrificedCard) implements TriggerContext {}

    /** Context for opponent-nontoken-permanent-sacrificed triggers. */
    record OpponentPermanentSacrificed(UUID sacrificingPlayerId, Card sacrificedCard) implements TriggerContext {}

    /** Context for global permanent-sacrificed triggers. */
    record PermanentSacrificed(UUID sacrificingPlayerId, Card sacrificedCard) implements TriggerContext {}

    /**
     * Context for dealt-damage-to-creature triggers (ON_DEALT_DAMAGE).
     */
    record DamageToCreature(Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId) implements TriggerContext {}

    record OpponentPermanentDealtExcessDamage(Permanent damagedPermanent,
                                              UUID damagedPermanentControllerId,
                                              int excessDamage) implements TriggerContext {}

    /** Context for a creature dealing damage to another creature. */
    record CreatureDealsDamageToCreature(Permanent damageSource, UUID damagedCreatureId,
                                          int damageDealt, boolean combatDamage) implements TriggerContext {}

    /** Context for a source dealing noncombat damage to a creature. */
    record SourceDealsNoncombatDamageToCreature(Permanent damagedCreature, int damageDealt,
                                                 UUID sourceControllerId) implements TriggerContext {}

    /** Context for a permanent becoming saddled. */
    record SelfBecomesSaddled(UUID controllerId) implements TriggerContext {}

    /** Context for global creature-damage triggers (ON_ANY_CREATURE_DEALT_DAMAGE). */
    record AnyCreatureDealtDamage(Permanent damagedCreature, UUID damagedCreatureControllerId,
                                  int damageDealt) implements TriggerContext {}

    /**
     * Context for enchanted-permanent-tap triggers (ON_ENCHANTED_PERMANENT_TAPPED).
     */
    record EnchantedPermanentTap(Permanent tappedPermanent, UUID tappedPermanentControllerId) implements TriggerContext {}

    /**
     * Context for life-loss triggers (ON_OPPONENT_LOSES_LIFE).
     */
    record LifeLoss(UUID losingPlayerId, int lifeLostAmount) implements TriggerContext {}

    /** Context for life-payment triggers (ON_CONTROLLER_PAYS_LIFE). */
    record LifePayment(UUID payingPlayerId, int lifePaidAmount) implements TriggerContext {}

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

    /** Context for controller-energy-gain triggers. */
    record EnergyGain(UUID gainingPlayerId, int energyGainedAmount) implements TriggerContext {}

    /** Context for a controller proliferating. */
    record Proliferate(UUID proliferatingPlayerId) implements TriggerContext {}

    /** Context for one counter-placement event caused by a player. */
    record CountersPlaced(UUID placingPlayerId, int amount) implements TriggerContext {}

    /** Context for loyalty-counter-removal triggers. */
    record LoyaltyCountersRemoved(Permanent permanent, int amount) implements TriggerContext {}

    /** Context for triggers that fire when a player wins a coin flip. */
    record CoinFlipWon(UUID winningPlayerId) implements TriggerContext {}

    /** Context for triggers that fire when a player loses a coin flip. */
    record CoinFlipLost(UUID losingPlayerId) implements TriggerContext {}

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

    /** Context for "whenever this creature or another creature you control is turned face up" triggers. */
    record PermanentTurnsFaceUp(Permanent turnedPermanent, UUID controllerId) implements TriggerContext {}

    /** Context for a permanent controlled by a player transforming. */
    record PermanentTransforms(Permanent transformedPermanent, Card transformedCard, UUID controllerId)
            implements TriggerContext {}

    // ── Death / leaves-battlefield contexts ────────────────────────────

    /**
     * Context for a card's own death triggers (ON_DEATH).
     * The dying permanent may be null when the 4-arg overload is used.
     */
    /** Context for one token-creation event, preserving the number of tokens that entered together. */
    record TokensEnter(int count, int perEffectTriggerCount) implements TriggerContext {
        public TokensEnter(int count) {
            this(count, 1);
        }
    }

    record SelfDeath(Card dyingCard, UUID controllerId, boolean wasCreature,
                     Permanent dyingPermanent, Card castingSpell) implements TriggerContext {
        public SelfDeath(Card dyingCard, UUID controllerId, boolean wasCreature,
                         Permanent dyingPermanent) {
            this(dyingCard, controllerId, wasCreature, dyingPermanent, null);
        }

        @Override
        public boolean causedByCreatureDying() {
            return wasCreature;
        }
    }

    /**
     * Context for creature-death triggers that reference the dying creature's card and controller.
     * Shared by ON_ALLY_CREATURE_DIES, ON_ANY_CREATURE_DIES, ON_ALLY_NONTOKEN_CREATURE_DIES,
     * ON_ANY_NONTOKEN_CREATURE_DIES, and ON_OPPONENT_CREATURE_DIES. {@code dyingCreaturePower} is the
     * dying creature's last-known effective power on the battlefield (Kresh the Bloodbraided) and
     * {@code dyingCreatureToughness} its last-known effective toughness (Grim Feast).
     * {@code dyingPermanent} preserves the dying permanent's counter state for trigger collectors
     * that need last-known counters.
     */
    record CreatureDeath(Card dyingCard, UUID dyingCreatureControllerId, int dyingCreaturePower,
                         int dyingCreatureToughness, UUID dyingPermanentId,
                         Permanent dyingPermanent, boolean wasCreature) implements TriggerContext {

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
     * Context for ON_EQUIPPED_CREATURE_DIES triggers. {@code dyingCard} is the card that died,
     * needed by effects that act on it in the graveyard (Oathkeeper, Takeno's Daisho).
     */
    record EquippedCreatureDeath(UUID dyingCreatureId,
                                 UUID dyingCreatureControllerId,
                                 Card dyingCard) implements TriggerContext {

        @Override
        public boolean causedByCreatureDying() {
            return true;
        }
    }

    /**
     * Context for ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD triggers.
     * {@code dyingCreaturePower} / {@code dyingCreatureToughness} are last-known effective P/T
     * (Death Watch uses power for life loss and toughness for life gain; Banewasp Affliction /
     * Creature Bond use toughness).
     */
    record EnchantedPermanentDeath(UUID dyingPermanentId, UUID dyingPermanentControllerId,
                                   UUID dyingCreatureCardId, int dyingCreaturePower,
                                   int dyingCreatureToughness, boolean wasCreature) implements TriggerContext {
        public EnchantedPermanentDeath(UUID dyingPermanentId, UUID dyingPermanentControllerId,
                                       UUID dyingCreatureCardId, int dyingCreaturePower,
                                       int dyingCreatureToughness) {
            this(dyingPermanentId, dyingPermanentControllerId, dyingCreatureCardId,
                    dyingCreaturePower, dyingCreatureToughness, true);
        }

        @Override
        public boolean causedByCreatureDying() {
            return wasCreature;
        }
    }

    /**
     * Context for ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD triggers.
     *
     * @param leavingPermanent   the permanent that left the battlefield
     * @param leavingControllerId the player who controlled it as it left (last-known information)
     * @param destination         the zone it entered, or {@code null} for legacy callers
     */
    record EnchantedPermanentLeaves(Permanent leavingPermanent, UUID leavingControllerId,
                                    Zone destination) implements TriggerContext {

        public EnchantedPermanentLeaves(Permanent leavingPermanent, UUID leavingControllerId) {
            this(leavingPermanent, leavingControllerId, null);
        }
    }

    /**
     * Context for ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD and
     * ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD triggers.
     */
    record ArtifactGraveyard(UUID graveyardOwnerId,
                             UUID artifactControllerId,
                             Card artifactCard,
                             int artifactManaValue) implements TriggerContext {

        public ArtifactGraveyard(UUID graveyardOwnerId, UUID artifactControllerId) {
            this(graveyardOwnerId, artifactControllerId, null, 0);
        }

        public ArtifactGraveyard(UUID graveyardOwnerId, UUID artifactControllerId, Card artifactCard) {
            this(graveyardOwnerId, artifactControllerId, artifactCard,
                    artifactCard == null ? 0 : artifactCard.getManaValue());
        }
    }

    /**
     * Context for ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Dingus Egg).
     */
    record AnyLandGraveyard(UUID graveyardOwnerId,
                            UUID landControllerId) implements TriggerContext {}

    /**
     * Context for ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers
     * (Femeref Enchantress).
     */
    record EnchantmentGraveyard(UUID graveyardOwnerId,
                                UUID enchantmentControllerId) implements TriggerContext {}

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
     * Context for ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers
     * (Kothophed, Soul Hoarder).
     *
     * @param dyingCard the permanent's card, now in its owner's graveyard
     * @param ownerId   the owner of the permanent (and of the graveyard it went to)
     */
    record OtherPlayerOwnedPermanentGraveyard(Card dyingCard, UUID ownerId) implements TriggerContext {}

    /**
     * Context for ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Yomiji, Who Bars
     * the Way).
     *
     * @param dyingCard          the permanent's card, now in {@code graveyardOwnerId}'s graveyard
     * @param dyingControllerId  the player who controlled the permanent on the battlefield
     * @param graveyardOwnerId   the owner of the graveyard the card was put into
     */
    record AnyPermanentGraveyard(Card dyingCard, UUID dyingControllerId,
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

    /** Context for controller-graveyard triggers that care about any non-token card. */
    record CardPutIntoGraveyard(Card card, UUID graveyardOwnerId) implements TriggerContext {}

    /** Context for ON_ALLY_LAND_CARD_MILLED triggers (Pedantic Learning). */
    record LandCardMilled(Card landCard, UUID graveyardOwnerId) implements TriggerContext {}

    /** Context for ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY triggers (Sidisi, Brood Tyrant). */
    record CreatureCardsPutIntoGraveyardFromLibrary(UUID graveyardOwnerId, int creatureCardCount)
            implements TriggerContext {}

    /** Context for ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT triggers (Karmic Justice). */
    record NoncreaturePermanentDestroyed(Card destroyedCard, UUID destroyedControllerId,
                                         UUID causeControllerId) implements TriggerContext {}

    /**
     * Context for ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE triggers (Soulcipher Board).
     *
     * @param creatureCard      the creature card that was put into the graveyard from anywhere
     * @param graveyardOwnerId  the owner of the graveyard the card was put into
     */
    record CreatureCardPutIntoGraveyard(Card creatureCard, UUID graveyardOwnerId) implements TriggerContext {}

    /**
     * Context for ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE triggers.
     *
     * @param permanentCard     the permanent card that was put into the graveyard from anywhere
     * @param graveyardOwnerId  the owner of the graveyard the card was put into
     */
    record PermanentCardPutIntoGraveyard(Card permanentCard, UUID graveyardOwnerId) implements TriggerContext {}

    /**
     * Context for ON_SELF_LEAVES_BATTLEFIELD triggers.
     */
    record SelfLeaves(UUID controllerId, Zone destination) implements TriggerContext {
        public SelfLeaves(UUID controllerId) {
            this(controllerId, Zone.GRAVEYARD);
        }
    }

    /** Context for a permanent becoming monstrous. */
    record SelfBecomesMonstrous(UUID controllerId, int xValue) implements TriggerContext {
        public SelfBecomesMonstrous(UUID controllerId) {
            this(controllerId, 0);
        }
    }

    /** Context for a permanent becoming untapped. */
    record SelfBecomesUntapped(UUID controllerId) implements TriggerContext {}

    /**
     * Context for ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers.
     */
    record AllyAuraOrEquipmentGraveyard(Card dyingCard,
                                        UUID controllerId) implements TriggerContext {}

    /**
     * Context for ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD triggers.
     */
    record ControllerCardsLeaveGraveyard(UUID graveyardOwnerId) implements TriggerContext {}

    /** Context for cards exiled from the controller's graveyard, including the event's card count. */
    record ControllerCardsExiledFromGraveyard(UUID graveyardOwnerId, int count) implements TriggerContext {}

    /** Context for Kaya's creature and creature-card exile trigger. */
    record ControllerCreaturesOrCreatureCardsExiled(UUID controllerId, int count,
                                                     List<Card> creatureCards) implements TriggerContext {
        public ControllerCreaturesOrCreatureCardsExiled {
            creatureCards = List.copyOf(creatureCards);
        }
    }

    /** Context for cards exiled from graveyards and/or the battlefield during the active player's turn. */
    record CardsExiledFromGraveyardsOrBattlefield(int count) implements TriggerContext {}

    record CardsExiledDuringTurn(UUID activePlayerId) implements TriggerContext {}

    /**
     * Context for ON_ANY_SOURCE_DEALS_DAMAGE triggers. Carries the damage source object, its
     * controller, the source permanent when applicable, and the total damage the source dealt in
     * this event (already summed across every simultaneous target).
     */
    record SourceDealsDamage(Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId,
                             int totalDamage, Map<UUID, Integer> damageToPlayers) implements TriggerContext {
        public SourceDealsDamage(Card sourceCard, UUID sourceControllerId, int totalDamage) {
            this(sourceCard, sourceControllerId, null, totalDamage, Map.of());
        }

        public SourceDealsDamage(Card sourceCard, UUID sourceControllerId, int totalDamage,
                                 Map<UUID, Integer> damageToPlayers) {
            this(sourceCard, sourceControllerId, null, totalDamage, damageToPlayers);
        }
    }

    /** Context for a source's combat-damage-only self trigger. */
    record SourceDealsCombatDamage(Card sourceCard, UUID sourceControllerId,
                                   UUID sourcePermanentId, int totalDamage,
                                   int damageToPlayers) implements TriggerContext {
        public SourceDealsCombatDamage(Card sourceCard, UUID sourceControllerId,
                                       UUID sourcePermanentId, int totalDamage) {
            this(sourceCard, sourceControllerId, sourcePermanentId, totalDamage, totalDamage);
        }
    }

    record CreatureDealsDamageToPlaneswalker(Permanent damageSource, UUID damagedPlaneswalkerId,
                                              int damage, boolean combatDamage,
                                              java.util.List<StackEntry> deferredTriggers)
            implements TriggerContext {

        public CreatureDealsDamageToPlaneswalker(Permanent damageSource, UUID damagedPlaneswalkerId,
                                                  int damage, boolean combatDamage) {
            this(damageSource, damagedPlaneswalkerId, damage, combatDamage, null);
        }
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
                                            UUID sourcePermanentId, UUID damagedPlayerId)
            implements TriggerContext {}

    record Crime(UUID committingPlayerId) implements TriggerContext {}
}
