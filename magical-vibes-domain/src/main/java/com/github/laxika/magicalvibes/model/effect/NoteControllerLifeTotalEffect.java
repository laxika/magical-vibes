package com.github.laxika.magicalvibes.model.effect;

/**
 * Notes the resolving source permanent's controller's current life total on that permanent.
 * The note is stored in the permanent's existing chosen-number state so it survives between
 * triggered abilities without adding card-specific state.
 */
public record NoteControllerLifeTotalEffect() implements ReplacementEffect {
}
