package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** A modal choice that is made while a triggered ability is being put on the stack. */
public interface TriggeredModalEffect extends CardEffect {

    ChooseOneEffect choice();

    @Override
    default ChooseOneEffect modesChosenAsItTriggers() {
        return choice();
    }

    /**
     * The most modes that may be chosen ("choose up to that many —"), an amount read as the ability is put on the
     * stack, or {@code null} for the modal's own count.
     */
    default DynamicAmount maximumChoices() {
        return null;
    }

    @Override
    default DynamicAmount maximumModesChosenAsItTriggers() {
        return maximumChoices();
    }

    /**
     * The modal of an ability made of {@code effect} alone, whose modes its controller chooses as the ability is put
     * on the stack (CR 700.2b): a {@link TriggeredModalEffect}'s choice, a {@link ChooseOneEffect} itself; {@code
     * null} for an effect that is not modal.
     */
    static ChooseOneEffect modesOf(CardEffect effect) {
        return switch (effect) {
            case TriggeredModalEffect modal -> modal.choice();
            case ChooseOneEffect modal -> modal;
            case null, default -> null;
        };
    }

    /** {@link #maximumChoices()} of the modal ability made of {@code effect} alone, or {@code null}. */
    static DynamicAmount maximumModesOf(CardEffect effect) {
        return effect instanceof TriggeredModalEffect modal ? modal.maximumChoices() : null;
    }

    /**
     * The modal whose mode is chosen as {@code effect}'s triggered ability is put on the stack (CR 603.3c), by the
     * trigger-time mode choice: a {@link TriggeredModalEffect}'s, or a {@link ChooseOneEffect} whose modes depend on
     * what its source chose before ({@link ChooseOneEffect#modeRestriction()}: "choose one that hasn't been chosen");
     * {@code null} for any other effect.
     */
    static ChooseOneEffect choiceMadeAsTriggered(CardEffect effect) {
        return effect == null ? null : effect.modesChosenAsItTriggers();
    }
}
