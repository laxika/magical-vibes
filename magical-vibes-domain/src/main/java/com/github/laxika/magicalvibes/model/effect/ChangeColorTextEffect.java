package com.github.laxika.magicalvibes.model.effect;

/**
 * "Change the text of target [spell or] permanent by replacing all instances of one [color/basic land
 * type/creature type] word with another." Resolves by prompting the controller for a from-word and a to-word, then
 * recording a {@code TextReplacement} on the target (CR 612 / 613.1c layer 3 text change).
 *
 * @param colorWordsAllowed when true the word swap may change color words (Mind Bend, Glamerdye);
 *                          when false color words are not offered (Magical Hack)
 * @param landTypesAllowed when true the word swap may change basic land type words (Mind Bend,
 *                         Magical Hack); when false only color words may be swapped (Glamerdye)
 * @param creatureTypesAllowed when true the word swap may change creature type words (Artificial
 *                             Evolution); the replacement creature type cannot be Wall
 * @param canTargetSpell   when true the effect may also target a spell on the stack in addition to a
 *                         permanent (Glamerdye, Magical Hack). A text change made to a permanent spell
 *                         carries to the permanent it becomes (CR 400.7a). The spell capability is
 *                         independent of {@link #targetSpec()} (which describes only the permanent
 *                         target), so it is kept on this dedicated record component and read through
 *                         {@code EffectResolution.targetsSpellOnStack(effect)}. Spell targets are
 *                         validated on the stack path ({@code checkSpellTargetOnStack}), never by the
 *                         spec interpreter.
 * @param untilEndOfTurn   when true the recorded {@code TextReplacement} wears off at the cleanup step
 *                         (Whim of Volrath); when false it lasts as long as the permanent remains on
 *                         the battlefield (Mind Bend, Magical Hack, Glamerdye).
 */
public record ChangeColorTextEffect(boolean colorWordsAllowed, boolean landTypesAllowed, boolean canTargetSpell,
                                    boolean untilEndOfTurn, boolean creatureTypesAllowed) implements CardEffect {

    public ChangeColorTextEffect(boolean colorWordsAllowed, boolean landTypesAllowed, boolean canTargetSpell) {
        this(colorWordsAllowed, landTypesAllowed, canTargetSpell, false, false);
    }

    public ChangeColorTextEffect(boolean colorWordsAllowed, boolean landTypesAllowed, boolean canTargetSpell,
                                 boolean untilEndOfTurn) {
        this(colorWordsAllowed, landTypesAllowed, canTargetSpell, untilEndOfTurn, false);
    }

    public static ChangeColorTextEffect creatureTypes(boolean canTargetSpell) {
        return new ChangeColorTextEffect(false, false, canTargetSpell, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
