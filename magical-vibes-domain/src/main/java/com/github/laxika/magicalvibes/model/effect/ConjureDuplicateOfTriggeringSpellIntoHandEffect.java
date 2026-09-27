package com.github.laxika.magicalvibes.model.effect;

/** Conjures a token-card duplicate of the spell that caused this trigger into its controller's hand. */
public record ConjureDuplicateOfTriggeringSpellIntoHandEffect(boolean discardAtNextEndStep)
        implements TriggeringSpellReferencingEffect {
}
