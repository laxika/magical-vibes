package com.github.laxika.magicalvibes.model.effect;

/**
 * "Create a token that's a copy of that artifact or creature" / "of one of those permanents": a token copy of a
 * permanent the spell that triggered the resolving cast trigger targets (the stack entry's triggering spell). Which
 * spells trigger it is the cast trigger's filter ({@code Triggers.youCastSpell().withStackEntryFilter(...)}). When
 * the spell targets several such permanents, the controller chooses one as the ability resolves.
 *
 * @param copyEffect                    the token copy to create ("except it's not legendary")
 * @param requiresTargetStillControlled {@code true} for "if it targets one or more other permanents you control,
 *                                      create a token that's a copy of one of those permanents" (Orvar, the
 *                                      All-Form): the spell's targets are read as the ability resolves (the live
 *                                      spell, else as it last existed on the stack), and only a target other than
 *                                      the source still on the battlefield under the ability controller's control
 *                                      can be copied (Orvar's rulings, 2021-02-05); {@code false} for "a copy of that
 *                                      artifact or creature" (Vesuvan Duplimancy): the spell's targets as the
 *                                      ability triggered, a target that changed control or left the battlefield
 *                                      copied as it last existed there (CR 608.2h; Vesuvan Duplimancy's rulings,
 *                                      2022-09-09)
 */
public record CreateTokenCopyOfTargetedSpellPermanentEffect(
        CreateTokenCopyOfTargetPermanentEffect copyEffect,
        boolean requiresTargetStillControlled) implements CardEffect {

    public CreateTokenCopyOfTargetedSpellPermanentEffect {
        if (copyEffect == null) {
            copyEffect = new CreateTokenCopyOfTargetPermanentEffect();
        }
    }

    /** "Create a token that's a copy of one of those permanents" (Orvar, the All-Form). */
    public CreateTokenCopyOfTargetedSpellPermanentEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect(), true);
    }

    /** "Create a token that's a copy of that [permanent], except …" (Vesuvan Duplimancy). */
    public CreateTokenCopyOfTargetedSpellPermanentEffect(CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        this(copyEffect, false);
    }
}
