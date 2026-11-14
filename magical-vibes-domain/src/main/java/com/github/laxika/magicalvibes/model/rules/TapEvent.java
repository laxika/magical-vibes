package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A permanent became tapped or untapped, or was tapped for mana (CR 106.12): the events the tap, untap and
 * tapped-for-mana abilities trigger on (CR 603.2). Each is an occurrence of its own, emitted as the permanent's status
 * changes or as its mana ability is activated. The permanents an event names are the live objects, which the abilities
 * read as they trigger; a simulation copy of the event copies them.
 */
public sealed interface TapEvent extends RulesEvent {

    /**
     * {@code permanent} became tapped, by {@code tappingPlayerId} (or, when {@code tappedByResolvingController}, by the
     * controller of the spell or ability resolving as it happened).
     */
    record BecameTapped(Permanent permanent, UUID tappingPlayerId, boolean tappedByResolvingController)
            implements TapEvent {

        public BecameTapped {
            Objects.requireNonNull(permanent, "permanent");
        }

        @Override
        public BecameTapped deepCopy() {
            return new BecameTapped(new Permanent(permanent), tappingPlayerId, tappedByResolvingController);
        }
    }

    /** {@code permanent} became untapped. */
    record BecameUntapped(Permanent permanent) implements TapEvent {

        public BecameUntapped {
            Objects.requireNonNull(permanent, "permanent");
        }

        @Override
        public BecameUntapped deepCopy() {
            return new BecameUntapped(new Permanent(permanent));
        }
    }

    /** {@code controllerId} untapped {@code untappedCount} permanents during their untap step. */
    record UntappedDuringUntapStep(UUID controllerId, int untappedCount) implements TapEvent {

        @Override
        public UntappedDuringUntapStep deepCopy() {
            return this;
        }
    }

    /** {@code creature} was tapped to pay a teamwork cost. */
    record TappedForTeamwork(Permanent creature) implements TapEvent {

        @Override
        public TappedForTeamwork deepCopy() {
            return new TappedForTeamwork(new Permanent(creature));
        }
    }

    /** {@code permanent}, which {@code controllerId} controls, was tapped for mana: its own "whenever you tap this". */
    record SelfTappedForMana(Permanent permanent, UUID controllerId) implements TapEvent {

        @Override
        public SelfTappedForMana deepCopy() {
            return new SelfTappedForMana(new Permanent(permanent), controllerId);
        }
    }

    /**
     * {@code tappingPlayerId} tapped the land {@code landId} for mana, producing {@code producedColors} (empty when not
     * known).
     */
    record LandTappedForMana(UUID tappingPlayerId, UUID landId, Set<ManaColor> producedColors) implements TapEvent {

        public LandTappedForMana {
            producedColors = Set.copyOf(producedColors);
        }

        @Override
        public LandTappedForMana deepCopy() {
            return this;
        }
    }

    /** {@code tappingPlayerId} tapped the creature {@code creatureId} for mana. */
    record CreatureTappedForMana(UUID tappingPlayerId, UUID creatureId) implements TapEvent {

        @Override
        public CreatureTappedForMana deepCopy() {
            return this;
        }
    }

    /**
     * {@code tappingPlayerId} tapped {@code permanent} for mana; {@code manaBefore} is their mana pool before the
     * ability, and {@code manaTypeChoicePending} whether the produced mana's type is still to be chosen.
     *
     * @param anyPermanent whether this is the event "a player taps a permanent for mana" (every permanent) rather than
     *                     "you tap a nonland permanent for mana"
     */
    record PermanentTappedForMana(UUID tappingPlayerId, Permanent permanent, Map<ManaColor, Integer> manaBefore,
                                  boolean manaTypeChoicePending, boolean anyPermanent) implements TapEvent {

        public PermanentTappedForMana {
            Objects.requireNonNull(permanent, "permanent");
            manaBefore = manaBefore == null ? Map.of() : Map.copyOf(manaBefore);
        }

        @Override
        public PermanentTappedForMana deepCopy() {
            return new PermanentTappedForMana(tappingPlayerId, new Permanent(permanent), manaBefore,
                    manaTypeChoicePending, anyPermanent);
        }
    }
}
