package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a source", "a source of your choice", "choose a source you control": as the effect resolves its controller
 * chooses a source of damage (CR 609.7a): a permanent, a spell on the stack, or any object referred to by an object on
 * the stack or by a prevention or redirection effect still waiting to apply, even one no longer in the zone it was in.
 * A source needn't be able to deal damage. Nothing is targeted. With one candidate it is chosen unasked (CR 608.2d).
 *
 * <p>A CHOOSE step that pauses at its end on the choice: the steps after it read the chosen object's id through
 * {@code Subjects.thisWay(StepKind.CHOOSE)} (a spell is recorded by its card's id, so a shield keyed to it also covers
 * the permanent that spell becomes, CR 609.7a).
 *
 * @param controlledBy whose sources may be chosen ("a source you control"): a permanent or a spell that player controls,
 *                     or an ability source whose damage that player controls; {@code null} for any source
 */
public record ChooseDamageSourceEffect(PlayerSubject controlledBy) implements CardEffect {

    /** "A source of your choice". */
    public static ChooseDamageSourceEffect any() {
        return new ChooseDamageSourceEffect(null);
    }

    /** "Choose a source you control". */
    public static ChooseDamageSourceEffect youControl() {
        return new ChooseDamageSourceEffect(Subjects.you());
    }
}
