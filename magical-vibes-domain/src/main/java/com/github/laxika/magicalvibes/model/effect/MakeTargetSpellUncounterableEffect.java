package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target spell can't be countered." Resolves by marking the targeted spell's card id as
 * uncounterable for as long as it stays on the stack (e.g. Vexing Shusher's activated ability).
 * The mark is read back by {@code GameQueryService.isUncounterable}.
 */
public record MakeTargetSpellUncounterableEffect() implements CardEffect {

    @Override
    public boolean canTargetSpell() {
        return true;
    }
}
