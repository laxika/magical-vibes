package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;
import java.util.UUID;

/**
 * Something happened to a spell or an ability, or a land was played: the events "whenever [a player] casts a spell",
 * "copies a spell", "counters a spell", "a spell you control is countered", "when this spell is countered or doesn't
 * resolve", "a spell you control causes you to gain card advantage", "plays a land", "activates an ability" and
 * "whenever a mana ability of this creature resolves" trigger on. Each is an occurrence of its own (CR 603.2c), emitted
 * where the action is complete: a spell becomes cast (CR 601.2i), an ability is activated (CR 602.2b), a land is
 * played (CR 305.1).
 *
 * <p>The events name the live objects (the spell on the stack, the activated ability's stack entry, the permanent whose
 * mana ability resolved), which the abilities that trigger record; a simulation copy of a pending event copies them.
 */
public sealed interface SpellEvent extends RulesEvent {

    /**
     * A spell became cast (CR 601.2i).
     *
     * @param spellCard               the spell's card
     * @param castingPlayerId         the player who cast it
     * @param castZone                the zone it was cast from
     * @param exiledSourcePermanentId for a card cast from exile, the permanent it was exiled with, or {@code null}
     */
    record Cast(Card spellCard, UUID castingPlayerId, Zone castZone, UUID exiledSourcePermanentId)
            implements SpellEvent {

        public Cast {
            Objects.requireNonNull(spellCard, "spellCard");
            Objects.requireNonNull(castingPlayerId, "castingPlayerId");
        }

        @Override
        public Cast deepCopy() {
            return this;
        }
    }

    /** A player copied a spell (CR 707.10): {@code copy} is the copy on the stack. */
    record Copied(StackEntry copy, UUID copyingPlayerId) implements SpellEvent {

        @Override
        public Copied deepCopy() {
            return new Copied(new StackEntry(copy), copyingPlayerId);
        }
    }

    /** A spell or ability {@code counteringPlayerId} controls countered a spell (CR 701.6a). */
    record Countered(UUID counteringPlayerId) implements SpellEvent {

        @Override
        public Countered deepCopy() {
            return this;
        }
    }

    /** A spell {@code spellControllerId} controls was countered. */
    record SpellCountered(UUID spellControllerId) implements SpellEvent {

        @Override
        public SpellCountered deepCopy() {
            return this;
        }
    }

    /**
     * An instant or sorcery spell was countered or left the stack without resolving (CR 608.2b): {@code spell} as it last
     * existed on the stack.
     */
    record CounteredOrFizzled(StackEntry spell) implements SpellEvent {

        @Override
        public CounteredOrFizzled deepCopy() {
            return new CounteredOrFizzled(new StackEntry(spell));
        }
    }

    /** {@code spell} finished resolving and gave its controller card advantage. */
    record CardAdvantage(StackEntry spell) implements SpellEvent {

        @Override
        public CardAdvantage deepCopy() {
            return new CardAdvantage(new StackEntry(spell));
        }
    }

    /**
     * A player played a land (CR 305.1), not one an effect put onto the battlefield.
     *
     * @param playingPlayerId         the player who played it
     * @param landCard                the land's card
     * @param playZone                the zone it was played from
     * @param exiledSourcePermanentId for a land played from exile, the permanent it was exiled with, or {@code null}
     */
    record LandPlayed(UUID playingPlayerId, Card landCard, Zone playZone, UUID exiledSourcePermanentId)
            implements SpellEvent {

        @Override
        public LandPlayed deepCopy() {
            return this;
        }
    }

    /**
     * An activated ability was activated: it is on the stack and its costs are paid (CR 602.2b, 601.2i).
     *
     * @param activatingPlayerId             the player who activated it
     * @param abilityEntry                   the ability on the stack; for a mana ability, which doesn't use the stack,
     *                                       the entry it would have been
     * @param ability                        the activated ability
     * @param activatedPermanent             the permanent whose ability it is, as it last existed when a cost removed
     *                                       it, or {@code null} for an ability of a card that isn't a permanent
     * @param activatedPermanentControllerId that permanent's controller as the activation began
     * @param manaAbility                    whether it is a mana ability (CR 605.1a)
     * @param lifePaid                       the life paid to activate it
     * @param treasureManaSpent              whether mana from a Treasure was spent to activate it
     */
    record Activated(UUID activatingPlayerId, StackEntry abilityEntry, ActivatedAbility ability,
                     Permanent activatedPermanent, UUID activatedPermanentControllerId, boolean manaAbility,
                     int lifePaid, boolean treasureManaSpent) implements SpellEvent {

        @Override
        public Activated deepCopy() {
            return new Activated(activatingPlayerId, abilityEntry == null ? null : new StackEntry(abilityEntry),
                    ability, activatedPermanent == null ? null : new Permanent(activatedPermanent),
                    activatedPermanentControllerId, manaAbility, lifePaid, treasureManaSpent);
        }
    }

    /**
     * A mana ability of {@code source} resolved, activated by {@code activatingPlayerId}, producing {@code manaProduced}
     * mana.
     */
    record ManaAbilityResolved(Permanent source, UUID activatingPlayerId, int manaProduced) implements SpellEvent {

        @Override
        public ManaAbilityResolved deepCopy() {
            return new ManaAbilityResolved(new Permanent(source), activatingPlayerId, manaProduced);
        }
    }
}
