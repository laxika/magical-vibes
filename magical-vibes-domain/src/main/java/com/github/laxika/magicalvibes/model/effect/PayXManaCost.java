package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import java.util.Objects;

/**
 * "Pay {@code manaCost}", a cost with an {X} whose value the payer chooses while paying it as the effect resolves
 * (CR 107.3f): "you may pay {X}{R}. If you do, it deals X damage to any target". The ransom of an
 * {@link UnlessPaysEffect} paid by its controller ({@link UnlessPaysEffect#youMayPayX}): the payer is asked for X
 * (0 is not paying), the cost with that X is paid as any resolution-time mana cost is, and the chosen X becomes the
 * resolving ability's X, so the branch and a reflexive "when you do" read it as {@code XValue}.
 *
 * @param manaCost the cost with its {X} symbol, such as {@code "{X}"} or {@code "{X}{W}{U}{B}"}
 * @param maximumX the most X may be ("X can't be greater than the amount of life you gained this turn"), read as the
 *                 effect resolves, or {@code null} for no limit
 */
public record PayXManaCost(String manaCost, DynamicAmount maximumX) implements CostEffect {

    public PayXManaCost {
        Objects.requireNonNull(manaCost, "manaCost");
        if (!manaCost.contains("{X}")) {
            throw new IllegalArgumentException("A cost whose X is chosen as it is paid has an {X}: " + manaCost);
        }
    }

    public PayXManaCost(String manaCost) {
        this(manaCost, null);
    }

    /** This cost with {@code x} for its X: the mana paid once X is chosen. */
    public String withX(int x) {
        return manaCost.replace("{X}", "{" + x + "}");
    }
}
