package com.github.laxika.magicalvibes.model.effect;

/**
 * A redirection of one source's damage this turn (CR 614.9): "the next time [source] would deal [combat] damage
 * [to recipients] this turn, it deals that damage to [destination] instead", or "all [combat] damage that would be
 * dealt [to recipients] this turn by [source] is dealt to [destination] instead" ({@link #allThisTurn()}). A
 * redirection, not a prevention, so it applies to damage that can't be prevented. The source is named as the effect resolves:
 * the source a {@link ChooseDamageSourceEffect} chose ({@code thisWay(CHOOSE)}: Opal-Eye, Reflect Damage), this
 * permanent ({@code source()}: Goblin Psychopath, Soltari Guerrillas), the target creature ({@code target()}: Mirror
 * Strike) or the target spell ({@code targetSpell()}: Reverberation); a spell also covers the permanent it becomes
 * (CR 609.7a), but not a later cast of its card. The redirection does nothing while the destination is no longer a
 * player in the game or a creature, planeswalker or battle on the battlefield, and the one-shot form waits for the
 * next damage it can redirect. A one-shot redirection that would send the damage where it is going anyway is used up
 * without moving it. Read by {@code DamagePreventionService.applicableSourceKeyedRedirects}.
 *
 * @param source        whose damage is redirected
 * @param recipients    whose damage is redirected, all players ("to you", "to an opponent"), fixed as the effect
 *                      resolves; {@code null} for damage to any player or permanent
 * @param toPermanent   the permanent the damage is dealt to instead ({@code source()}: "to Opal-Eye"; {@code target()}
 *                      declares a harmful creature target), or {@code null}
 * @param toPlayer      the player the damage is dealt to instead ("to you"), or {@code null}; with neither
 *                      destination, the source's controller as the damage would be dealt ("to its controller")
 * @param wholeTurn     whether all such damage this turn is redirected rather than the next time only
 * @param combatOnly    whether only combat damage is redirected
 */
public record RedirectNextDamageFromChosenSourceToSelfEffect(PermanentSubject source, PlayerSubject recipients,
                                                             PermanentSubject toPermanent, PlayerSubject toPlayer,
                                                             boolean wholeTurn, boolean combatOnly)
        implements CardEffect {

    /**
     * "The next time a source of your choice would deal damage this turn, that damage is dealt to this creature
     * instead" (Opal-Eye), after a {@link ChooseDamageSourceEffect}.
     */
    public RedirectNextDamageFromChosenSourceToSelfEffect() {
        this(Subjects.thisWay(com.github.laxika.magicalvibes.model.StepKind.CHOOSE), null, Subjects.source(), null,
                false, false);
    }

    /** "The next time [source] would deal damage this turn, it deals that damage to [permanent] instead." */
    public static RedirectNextDamageFromChosenSourceToSelfEffect to(PermanentSubject source, PermanentSubject permanent) {
        return new RedirectNextDamageFromChosenSourceToSelfEffect(source, null, permanent, null, false, false);
    }

    /** "The next time [source] would deal damage this turn, it deals that damage to [player] instead." */
    public static RedirectNextDamageFromChosenSourceToSelfEffect toPlayer(PermanentSubject source, PlayerSubject player) {
        return new RedirectNextDamageFromChosenSourceToSelfEffect(source, null, null, player, false, false);
    }

    /** "The next time [source] would deal damage this turn, that damage is dealt to that source's controller instead." */
    public static RedirectNextDamageFromChosenSourceToSelfEffect toItsController(PermanentSubject source) {
        return new RedirectNextDamageFromChosenSourceToSelfEffect(source, null, null, null, false, false);
    }

    /** This redirection, only of the source's damage to {@code players} ("to you", "to an opponent"). */
    public RedirectNextDamageFromChosenSourceToSelfEffect dealtTo(PlayerSubject players) {
        return new RedirectNextDamageFromChosenSourceToSelfEffect(source, players, toPermanent, toPlayer, wholeTurn,
                combatOnly);
    }

    /** This redirection, of all such damage this turn ("all damage that would be dealt this turn by …"). */
    public RedirectNextDamageFromChosenSourceToSelfEffect allThisTurn() {
        return new RedirectNextDamageFromChosenSourceToSelfEffect(source, recipients, toPermanent, toPlayer, true,
                combatOnly);
    }

    /** This redirection, of combat damage only. */
    public RedirectNextDamageFromChosenSourceToSelfEffect combatDamageOnly() {
        return new RedirectNextDamageFromChosenSourceToSelfEffect(source, recipients, toPermanent, toPlayer,
                wholeTurn, true);
    }

    /**
     * A targeted destination takes the damage, so it is a harmful creature target (CR 702.16b); a targeted source
     * (a creature or a spell) is a benign target, narrowed by the card's target filter.
     */
    @Override
    public TargetSpec targetSpec() {
        if (toPermanent != null && toPermanent.declaresTarget()) {
            return TargetSpec.harmful(TargetPredicates.creature());
        }
        if (source instanceof PermanentSubject.TargetSpell) {
            return TargetSpec.benign(TargetPredicates.spellOnStack());
        }
        return source.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
