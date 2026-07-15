package com.github.laxika.magicalvibes.model.effect;

/**
 * "Change the text of target [spell or] permanent by replacing all instances of one [color] word
 * with another." Resolves by prompting the controller for a from-word and a to-word, then recording
 * a {@code TextReplacement} on the target (CR 612 / 613.2c layer 3 text change).
 *
 * @param landTypesAllowed when true the word swap may also change basic land type words (Mind Bend);
 *                         when false only color words may be swapped (Glamerdye)
 * @param canTargetSpell   when true the effect may also target a spell on the stack in addition to a
 *                         permanent (Glamerdye). A text change made to a permanent spell carries to
 *                         the permanent it becomes (CR 613.7). This record component's accessor
 *                         overrides {@code CardEffect.canTargetSpell()}, so the spell capability is
 *                         independent of {@link #targetSpec()} (which describes only the permanent
 *                         target). Spell targets are validated on the stack path
 *                         ({@code checkSpellTargetOnStack}), never by the spec interpreter.
 */
public record ChangeColorTextEffect(boolean landTypesAllowed, boolean canTargetSpell) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
