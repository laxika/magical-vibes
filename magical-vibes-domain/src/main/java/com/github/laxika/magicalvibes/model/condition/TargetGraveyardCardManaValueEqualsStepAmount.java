package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Objects;

/**
 * The targeted graveyard card's mana value equals the amount the most recent {@code kind} step of this resolution
 * recorded: "you may pay {X}. If you do, return another target Zombie creature card with mana value X" (Relentless
 * Dead), where the target is chosen as the ability goes on the stack and X only as it resolves. A paid {0} is an
 * amount of 0 (CR 118.12); with no such step it is 0 too.
 *
 * @param kind the step whose amount the mana value is compared with ({@code PAY_MANA})
 */
public record TargetGraveyardCardManaValueEqualsStepAmount(StepKind kind) implements Condition {

    public TargetGraveyardCardManaValueEqualsStepAmount {
        Objects.requireNonNull(kind, "kind");
    }

    @Override
    public String conditionName() {
        return "target graveyard card's mana value equals the amount paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "target graveyard card's mana value differs from the amount paid";
    }
}
