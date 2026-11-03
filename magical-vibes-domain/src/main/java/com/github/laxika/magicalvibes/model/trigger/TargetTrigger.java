package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * A player or object becomes the target of a spell (CR 601.2c) or an activated or triggered ability (CR 602.2b,
 * CR 603.3d), or a player chooses targets for one (CR 115.1). Build it through {@link Triggers}.
 *
 * @param event     what targets: any spell, an Aura spell, an instant or sorcery spell, a spell or ability, one an
 *                  opponent of the source's controller controls or one its controller controls, an activated ability,
 *                  a backup ability; and what becomes the target when that isn't a permanent: you, you or a permanent
 *                  you control, a permanent or a spell, a player or permanent
 * @param relation  how the permanent that becomes the target relates to the ability's source:
 *                  {@link ObjectRelation#THIS} for "this permanent", {@link ObjectRelation#YOU_CONTROL} for "a
 *                  creature you control", {@link ObjectRelation#ANOTHER_YOU_CONTROL} for "another creature you
 *                  control", {@link ObjectRelation#ANY} for "a creature", {@link ObjectRelation#OPPONENT_CONTROLS} for
 *                  "a creature an opponent controls". An event about you alone, about any player or permanent, or
 *                  about a player choosing targets, is {@link ObjectRelation#ANY}
 * @param filter    what that permanent must be, or {@code null} for any permanent: a creature. The engine's
 *                  becomes-the-target collectors check it
 * @param qualifier what that permanent must also be, or {@code null}: "whenever a Dragon you control becomes the
 *                  target of a spell or ability an opponent controls" is {@code
 *                  Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility().withFilter(dragon)}. The
 *                  collectors read it from the legacy view ({@code TriggeredAbility.legacyEffects()}) as the
 *                  {@code TriggeringPermanentConditionalEffect} they judge, so {@link #watchKey()} drops it
 */
public record TargetTrigger(TargetEvent event, ObjectRelation relation, PermanentPredicate filter,
                            PermanentPredicate qualifier) implements Trigger {

    public TargetTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(relation, "relation");
    }

    /** A trigger on any permanent of {@code filter}'s type that relates to the source as {@code relation} says. */
    public TargetTrigger(TargetEvent event, ObjectRelation relation, PermanentPredicate filter) {
        this(event, relation, filter, null);
    }

    /** A trigger on any permanent that relates to the source as {@code relation} says. */
    public TargetTrigger(TargetEvent event, ObjectRelation relation) {
        this(event, relation, null, null);
    }

    /**
     * This trigger narrowed to a permanent that also matches {@code filter}, as {@link #qualifier} says: "whenever a
     * Sliver creature you control becomes the target of a spell or ability an opponent controls".
     */
    public TargetTrigger withFilter(PermanentPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (qualifier != null) {
            throw new IllegalStateException("This target trigger already has a filter: " + this);
        }
        return new TargetTrigger(event, relation, this.filter, filter);
    }

    /**
     * The trigger the {@code TargetWatch} collectors read this one's abilities by: without its {@link #qualifier},
     * and, for the events no collector of their own reads, with the event whose collector reads them. "This permanent
     * becomes the target of a spell you control" and "of a spell or ability you control" are read by the collectors of
     * the event about any controller ({@link TargetEvent#BECOMES_TARGET_OF_SPELL},
     * {@link TargetEvent#BECOMES_TARGET_OF_SPELL_OR_ABILITY}), and "a player or permanent becomes the target of an
     * ability you control" by the one of {@link TargetEvent#PLAYER_CHOOSES_TARGETS}. The legacy view ({@code
     * TriggeredAbility.legacyEffects()}) gives them the wrapper those collectors judge.
     */
    @Override
    public Trigger watchKey() {
        TargetEvent watched = watchedEvent();
        if (watched == event && qualifier == null) {
            return this;
        }
        return new TargetTrigger(watched, relation, filter);
    }

    /** The event of {@link #watchKey()}: {@link #event}, or the event whose collector reads this one's abilities. */
    public TargetEvent watchedEvent() {
        return switch (event) {
            case BECOMES_TARGET_OF_YOUR_SPELL -> relation == ObjectRelation.THIS
                    ? TargetEvent.BECOMES_TARGET_OF_SPELL : event;
            case BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY -> relation == ObjectRelation.THIS
                    ? TargetEvent.BECOMES_TARGET_OF_SPELL_OR_ABILITY : event;
            case PLAYER_OR_PERMANENT_BECOMES_TARGET_OF_YOUR_ABILITY -> TargetEvent.PLAYER_CHOOSES_TARGETS;
            default -> event;
        };
    }

    /**
     * What a {@link TargetTrigger} triggers on. Unless a value says "one or more", the ability triggers once for each
     * targeting spell or ability and each permanent it targets. On an Aura or Equipment the {@link ObjectRelation#THIS}
     * forms of {@link #BECOMES_TARGET_OF_SPELL}, {@link #BECOMES_TARGET_OF_AURA_SPELL},
     * {@link #BECOMES_TARGET_OF_SPELL_OR_ABILITY} and {@link #BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY} are the
     * permanent it is attached to ("whenever enchanted creature becomes the target", or the ability it grants that
     * permanent), as the slots they replace were.
     */
    public enum TargetEvent {
        /** A player chooses one or more targets for a spell or ability as it is put on the stack. */
        PLAYER_CHOOSES_TARGETS,
        /**
         * A player or permanent becomes the target of an activated or triggered ability the source's controller
         * controls.
         */
        PLAYER_OR_PERMANENT_BECOMES_TARGET_OF_YOUR_ABILITY,
        /** A permanent becomes the target of a spell, whoever controls it. */
        BECOMES_TARGET_OF_SPELL,
        /** A permanent becomes the target of a spell the source's controller controls. */
        BECOMES_TARGET_OF_YOUR_SPELL,
        /** A permanent becomes the target of an Aura spell, whoever controls it. */
        BECOMES_TARGET_OF_AURA_SPELL,
        /** A permanent becomes the target of an instant or sorcery spell, whoever controls it. */
        BECOMES_TARGET_OF_INSTANT_OR_SORCERY_SPELL,
        /** A permanent becomes the target of a spell an opponent of the source's controller controls. */
        BECOMES_TARGET_OF_OPPONENT_SPELL,
        /** A permanent becomes the target of a spell or ability, whoever controls it. */
        BECOMES_TARGET_OF_SPELL_OR_ABILITY,
        /**
         * A permanent becomes the target of a spell or ability an opponent of the source's controller controls; ward
         * (CR 702.21a) is this with {@link ObjectRelation#THIS}.
         */
        BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
        /** A permanent becomes the target of a spell or ability the source's controller controls. */
        BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
        /** One or more permanents become the target of an activated ability, once for each ability. */
        ONE_OR_MORE_BECOME_TARGET_OF_ACTIVATED_ABILITY,
        /** A permanent becomes the target of a backup ability (CR 702.165a). */
        BECOMES_TARGET_OF_BACKUP_ABILITY,
        /**
         * A permanent, or a spell the source's controller controls that matches the filter as a permanent would,
         * becomes the target of a spell or ability an opponent controls.
         */
        PERMANENT_OR_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
        /** The source's controller becomes the target of a spell, whoever controls it. */
        YOU_BECOME_TARGET_OF_SPELL,
        /** The source's controller becomes the target of a spell or ability an opponent controls. */
        YOU_BECOME_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
        /**
         * The source's controller or a permanent they control becomes the target of a spell or ability an opponent
         * controls.
         */
        YOU_OR_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY
    }
}
