package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static prevention effect (CR 615): "prevent all [combat | noncombat] damage that would be dealt to [recipients] [by
 * sources]", or "if a source would deal damage to [recipients], prevent N of that damage". Built through the factories
 * and the {@code with…} methods, e.g. Light of Sanction's {@code to(Subjects.creaturesYouControl()).fromSourcesYouControl()}
 * or Shield of the Realm's {@code to(Subjects.attachedCreature()).preventing(2)}.
 *
 * <p>Nothing is locked in: the recipients and the source are judged as each damage event would happen (CR 615.1), so a
 * permanent that enters, changes control or starts blocking later is covered once it matches. Read by
 * {@code DamagePreventionService.staticPreventionsOfPermanentDamage} / {@code staticPreventionsOfPlayerDamage} through
 * the CR 616.1 damage pools: one option for every prevention of all the damage, and one option per effect that prevents
 * an amount, so the affected player orders each against the other replacement and prevention effects. An amount is
 * prevented from each source's damage separately (combat damage from several sources is one event with a portion per
 * source). A static ability of a face-up plane isn't read.
 *
 * @param permanents        the permanents shielded (a static subject: {@code source()}, {@code attachedCreature()},
 *                          {@code controlledBy(...)}), or {@code null} for none
 * @param players           the players shielded ({@code you()}, {@code eachPlayer()}), or {@code null} for none
 * @param sources           the damage sources whose damage is prevented, judged on the source as it is, else as it last
 *                          existed, else (a spell) on its card, with the effect's permanent as the filter's source; or
 *                          {@code null} for any source
 * @param sourceControllers whose sources ("sources you control"), or {@code null} for anyone's
 * @param damage            which damage: all of it, only combat damage or only noncombat damage
 * @param sourceKind        whether only damage a spell deals ("if a spell would deal damage") is prevented
 * @param sourceOfChosenColor only damage from sources of the color the effect's permanent chose last, judged on the
 *                          source's colors as a source of damage
 * @param amount            how much of each source's damage is prevented, evaluated for the effect's permanent as the
 *                          damage would be dealt; {@code null} for all of it
 */
public record PreventDamageToRecipientsEffect(PermanentSubject permanents, PlayerSubject players,
                                              PermanentPredicate sources, PlayerSubject sourceControllers,
                                              DamageKind damage, SourceKind sourceKind, boolean sourceOfChosenColor,
                                              DynamicAmount amount) implements CardEffect {

    /** Which damage the prevention applies to. */
    public enum DamageKind {
        ALL,
        COMBAT,
        NONCOMBAT;

        /** Whether damage that is or isn't combat damage is this kind. */
        public boolean covers(boolean combat) {
            return this == ALL || (this == COMBAT) == combat;
        }
    }

    /** Which objects' damage the prevention applies to. */
    public enum SourceKind {
        /** Any source's. */
        ANY,
        /** Only damage a spell deals itself; not a permanent a spell makes fight or deal damage. */
        SPELL,
        /** Only damage an instant or sorcery spell deals itself. */
        INSTANT_OR_SORCERY_SPELL
    }

    public PreventDamageToRecipientsEffect {
        if (permanents == null && players == null) {
            throw new IllegalArgumentException("A prevention shields at least one permanent or player");
        }
        if (permanents != null && permanents.needsStackEntry()) {
            throw new IllegalArgumentException("A static prevention can't name " + permanents);
        }
        if (players != null && players.needsStackEntry()) {
            throw new IllegalArgumentException("A static prevention can't name " + players);
        }
        if (sourceControllers != null && sourceControllers.needsStackEntry()) {
            throw new IllegalArgumentException("A static prevention can't name " + sourceControllers);
        }
        if (damage == null) {
            damage = DamageKind.ALL;
        }
        if (sourceKind == null) {
            sourceKind = SourceKind.ANY;
        }
    }

    /** "Prevent all damage that would be dealt to {@code permanents}." */
    public static PreventDamageToRecipientsEffect to(PermanentSubject permanents) {
        return new PreventDamageToRecipientsEffect(permanents, null, null, null, DamageKind.ALL, SourceKind.ANY,
                false, null);
    }

    /** "Prevent all damage that would be dealt to you and {@code permanents}." */
    public static PreventDamageToRecipientsEffect toYouAnd(PermanentSubject permanents) {
        return new PreventDamageToRecipientsEffect(permanents, Subjects.you(), null, null, DamageKind.ALL,
                SourceKind.ANY, false, null);
    }

    /** "Prevent all damage that would be dealt" to every player and every permanent. */
    public static PreventDamageToRecipientsEffect toEveryone() {
        return new PreventDamageToRecipientsEffect(Subjects.allPermanents(), Subjects.eachPlayer(), null, null,
                DamageKind.ALL, SourceKind.ANY, false, null);
    }

    /** Only damage from sources matching {@code filter} ("by Deserts", "by creatures it's blocking"). */
    public PreventDamageToRecipientsEffect from(PermanentPredicate filter) {
        return new PreventDamageToRecipientsEffect(permanents, players, filter, sourceControllers, damage,
                sourceKind, sourceOfChosenColor, amount);
    }

    /** Only damage from sources you control. */
    public PreventDamageToRecipientsEffect fromSourcesYouControl() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, Subjects.you(), damage,
                sourceKind, sourceOfChosenColor, amount);
    }

    /** Only damage spells deal ("if a spell would deal damage"). */
    public PreventDamageToRecipientsEffect fromSpells() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers, damage,
                SourceKind.SPELL, sourceOfChosenColor, amount);
    }

    /** Only damage instant and sorcery spells deal. */
    public PreventDamageToRecipientsEffect fromInstantAndSorcerySpells() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers, damage,
                SourceKind.INSTANT_OR_SORCERY_SPELL, sourceOfChosenColor, amount);
    }

    /** Only damage from sources of the color this permanent chose ("sources of the chosen color"). */
    public PreventDamageToRecipientsEffect fromSourcesOfChosenColor() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers, damage,
                sourceKind, true, amount);
    }

    /** Only combat damage. */
    public PreventDamageToRecipientsEffect combatOnly() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers, DamageKind.COMBAT,
                sourceKind, sourceOfChosenColor, amount);
    }

    /** Only noncombat damage. */
    public PreventDamageToRecipientsEffect noncombatOnly() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers,
                DamageKind.NONCOMBAT, sourceKind, sourceOfChosenColor, amount);
    }

    /** "Prevent {@code prevented} of that damage" from each source, instead of all of it. */
    public PreventDamageToRecipientsEffect preventing(DynamicAmount prevented) {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers, damage,
                sourceKind, sourceOfChosenColor, prevented);
    }

    /** "Prevent {@code prevented} of that damage" from each source. */
    public PreventDamageToRecipientsEffect preventing(int prevented) {
        if (prevented <= 0) {
            throw new IllegalArgumentException("A prevented amount is positive");
        }
        return preventing(new Fixed(prevented));
    }
}
