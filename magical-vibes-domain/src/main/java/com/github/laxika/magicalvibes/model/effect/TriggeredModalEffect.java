package com.github.laxika.magicalvibes.model.effect;

/** A modal choice that is made while a triggered ability is being put on the stack. */
public interface TriggeredModalEffect extends CardEffect {

    ChooseOneEffect choice();

    /**
     * The modal whose mode is chosen as {@code effect}'s triggered ability is put on the stack (CR 603.3c), by the
     * trigger-time mode choice: a {@link TriggeredModalEffect}'s, or a {@link ChooseOneEffect} whose modes depend on
     * what its source chose before ({@link ChooseOneEffect#modeRestriction()}: "choose one that hasn't been chosen");
     * {@code null} for any other effect.
     */
    static ChooseOneEffect choiceMadeAsTriggered(CardEffect effect) {
        return switch (effect) {
            case TriggeredModalEffect modal -> modal.choice();
            case ChooseOneEffect modal when modal.modeRestriction() != ChooseOneEffect.ModeRestriction.NONE -> modal;
            case null, default -> null;
        };
    }
}
