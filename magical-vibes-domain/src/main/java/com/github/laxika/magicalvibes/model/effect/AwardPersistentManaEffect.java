package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/**
 * "Add {@code amount} mana … Until end of turn, you don't lose this mana as steps and phases end": the mana stays in
 * the recipient's mana pool through step and phase transitions until the end of the turn, an exception the text makes
 * to the pool emptying (CR 106.4). This is an effect of a triggered ability or a spell, not a mana ability.
 *
 * <p>With one color the mana is that color ("add {R}{R}"). With several, the recipient chooses: one color for all of
 * it ("add two mana of any one color", {@link #anyOneColor}), or a color for each mana ("add that much mana in any
 * combination of {R} and/or {G}", {@link #inAnyCombinationOf}).
 *
 * @param colors               the colors the mana may be (one: no choice)
 * @param amount               how much mana is added
 * @param recipient            who adds it
 * @param eachChosenSeparately whether each mana's color is chosen separately ("in any combination of")
 * @param restriction          {@link ManaSpendRestriction#NONE}, or {@link ManaSpendRestriction#SPELL_ONLY} for
 *                             "spend this mana only to cast spells" (five colors only)
 */
public record AwardPersistentManaEffect(List<ManaColor> colors, DynamicAmount amount, Recipient recipient,
                                        boolean eachChosenSeparately, ManaSpendRestriction restriction)
        implements CardEffect {

    public AwardPersistentManaEffect {
        colors = List.copyOf(colors);
        if (colors.isEmpty()) {
            throw new IllegalArgumentException("Persistent mana needs at least one color");
        }
        if (restriction != ManaSpendRestriction.NONE && restriction != ManaSpendRestriction.SPELL_ONLY) {
            throw new IllegalArgumentException("Persistent mana supports no spend restriction but spells only: "
                    + restriction);
        }
        if (restriction == ManaSpendRestriction.SPELL_ONLY && !colors.equals(ManaColor.COLORS)) {
            throw new IllegalArgumentException("Spell-only persistent mana is chosen among the five colors");
        }
    }

    /** "Add {@code amount} {@code color}" to the controller. */
    public AwardPersistentManaEffect(ManaColor color, DynamicAmount amount) {
        this(color, amount, Recipient.CONTROLLER);
    }

    /** "That player adds {@code amount} {@code color}" ({@link Recipient#TARGET_PLAYER}: Shizuko). */
    public AwardPersistentManaEffect(ManaColor color, DynamicAmount amount, Recipient recipient) {
        this(List.of(color), amount, recipient, false, ManaSpendRestriction.NONE);
    }

    /** "Add {@code amount} mana of any one color" (Branch of Vitu-Ghazi, Photon). */
    public static AwardPersistentManaEffect anyOneColor(DynamicAmount amount) {
        return new AwardPersistentManaEffect(ManaColor.COLORS, amount, Recipient.CONTROLLER, false,
                ManaSpendRestriction.NONE);
    }

    /** "Add {@code amount} mana of any one color" (Branch of Vitu-Ghazi). */
    public static AwardPersistentManaEffect anyOneColor(int amount) {
        return anyOneColor(new Fixed(amount));
    }

    /**
     * "Add {@code amount} mana in any combination of {@code colors}" (Grand Warlord Radha's {R} and/or {G}; Klauth's
     * colors): each mana's color is chosen separately.
     */
    public static AwardPersistentManaEffect inAnyCombinationOf(List<ManaColor> colors, DynamicAmount amount) {
        return new AwardPersistentManaEffect(colors, amount, Recipient.CONTROLLER, true, ManaSpendRestriction.NONE);
    }

    /** "Spend this mana only to cast spells" (Klauth). */
    public AwardPersistentManaEffect spendOnlyToCastSpells() {
        return new AwardPersistentManaEffect(colors, amount, recipient, eachChosenSeparately,
                ManaSpendRestriction.SPELL_ONLY);
    }

    public enum Recipient {
        CONTROLLER,
        TARGET_PLAYER
    }
}
