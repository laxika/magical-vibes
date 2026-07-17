package com.github.laxika.magicalvibes.model.effect;

/**
 * "Change the text of target [spell or] permanent by replacing all instances of one [color/basic land
 * type] word with another." Resolves by prompting the controller for a from-word and a to-word, then
 * recording a {@code TextReplacement} on the target (CR 612 / 613.2c layer 3 text change).
 *
 * @param colorWordsAllowed when true the word swap may change color words (Mind Bend, Glamerdye);
 *                          when false color words are not offered (Magical Hack)
 * @param landTypesAllowed when true the word swap may change basic land type words (Mind Bend,
 *                         Magical Hack); when false only color words may be swapped (Glamerdye)
 * @param canTargetSpell   when true the effect may also target a spell on the stack in addition to a
 *                         permanent (Glamerdye, Magical Hack). A text change made to a permanent spell
 *                         carries to the permanent it becomes (CR 613.7). The spell capability is
 *                         independent of {@link #targetSpec()} (which describes only the permanent
 *                         target), so it is kept on this dedicated record component and read through
 *                         {@code EffectResolution.targetsSpellOnStack(effect)}. Spell targets are
 *                         validated on the stack path ({@code checkSpellTargetOnStack}), never by the
 *                         spec interpreter.
 */
public record ChangeColorTextEffect(boolean colorWordsAllowed, boolean landTypesAllowed, boolean canTargetSpell)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
