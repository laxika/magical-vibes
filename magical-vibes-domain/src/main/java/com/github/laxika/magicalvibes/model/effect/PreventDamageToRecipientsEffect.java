package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static prevention effect (CR 615): "prevent all [combat | noncombat] damage that would be dealt to [recipients] [by
 * sources]". Built through the factories and the {@code with…} methods, e.g. Light of Sanction's
 * {@code to(Subjects.creaturesYouControl()).fromSourcesYouControl()}.
 *
 * <p>Nothing is locked in: the recipients and the source are judged as each damage event would happen (CR 615.1), so a
 * permanent that enters, changes control or starts blocking later is covered once it matches. Read by
 * {@code DamagePreventionService.staticallyPreventsDamageToPermanent} / {@code staticallyPreventsDamageToPlayer} through
 * the CR 616.1 damage pools, one option per pool, so the affected player orders it against the other replacement and
 * prevention effects. A static ability of a face-up plane isn't read.
 *
 * @param permanents        the permanents shielded (a static subject: {@code source()}, {@code attachedCreature()},
 *                          {@code controlledBy(...)}), or {@code null} for none
 * @param players           the players shielded ({@code you()}), or {@code null} for none
 * @param sources           the damage sources whose damage is prevented, judged on the source as it is, else as it last
 *                          existed, else (a spell) on its card, with the effect's permanent as the filter's source; or
 *                          {@code null} for any source
 * @param sourceControllers whose sources ("sources you control"), or {@code null} for anyone's
 * @param damage            which damage: all of it, only combat damage or only noncombat damage
 */
public record PreventDamageToRecipientsEffect(PermanentSubject permanents, PlayerSubject players,
                                              PermanentPredicate sources, PlayerSubject sourceControllers,
                                              DamageKind damage) implements CardEffect {

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
    }

    /** "Prevent all damage that would be dealt to {@code permanents}." */
    public static PreventDamageToRecipientsEffect to(PermanentSubject permanents) {
        return new PreventDamageToRecipientsEffect(permanents, null, null, null, DamageKind.ALL);
    }

    /** "Prevent all damage that would be dealt to you and {@code permanents}." */
    public static PreventDamageToRecipientsEffect toYouAnd(PermanentSubject permanents) {
        return new PreventDamageToRecipientsEffect(permanents, Subjects.you(), null, null, DamageKind.ALL);
    }

    /** Only damage from sources matching {@code filter} ("by Deserts", "by creatures it's blocking"). */
    public PreventDamageToRecipientsEffect from(PermanentPredicate filter) {
        return new PreventDamageToRecipientsEffect(permanents, players, filter, sourceControllers, damage);
    }

    /** Only damage from sources you control. */
    public PreventDamageToRecipientsEffect fromSourcesYouControl() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, Subjects.you(), damage);
    }

    /** Only combat damage. */
    public PreventDamageToRecipientsEffect combatOnly() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers, DamageKind.COMBAT);
    }

    /** Only noncombat damage. */
    public PreventDamageToRecipientsEffect noncombatOnly() {
        return new PreventDamageToRecipientsEffect(permanents, players, sources, sourceControllers,
                DamageKind.NONCOMBAT);
    }
}
