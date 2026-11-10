package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Objects;

/**
 * True when "that player" (the player the ability's event was about, or the targeted player) was attacked this turn by
 * a {@code subtype} the source's controller controlled: "if they were attacked this turn by an Assassin you
 * controlled" (Ramses, Assassin Lord). The attacker is judged as it was declared, so a later change of control or
 * subtype, or its leaving the battlefield, doesn't undo the attack.
 */
public record TargetPlayerAttackedBySubtypeControllerControlledThisTurn(CardSubtype subtype) implements Condition {

    public TargetPlayerAttackedBySubtypeControllerControlledThisTurn {
        Objects.requireNonNull(subtype, "subtype");
    }

    @Override
    public String conditionName() {
        return "that player was attacked this turn by a " + subtype + " you controlled";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player wasn't attacked this turn by a " + subtype + " you controlled";
    }
}
