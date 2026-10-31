package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factories for the mana abilities lands and mana rocks repeat verbatim. The reminder text
 * is derived from the colour, so a dual land's several abilities cannot drift out of sync
 * with what they actually produce.
 *
 * <p>Only unconditional "{@code {T}: Add …}" abilities belong here. Anything with a cost
 * beyond tapping, a restriction on what the mana may pay for, or a drawback (pain lands,
 * lands that enter tapped) is card-specific and should be built directly.
 */
public final class ManaAbilities {

    private ManaAbilities() {
    }

    /** {@code {T}: Add} one mana of {@code color}. */
    public static ActivatedAbility tapFor(ManaColor color) {
        return new ActivatedAbility(true, null,
                List.of(new AwardManaEffect(color)),
                "{T}: Add {" + color.getCode() + "}.");
    }

    /** {@code {T}: Add} {@code amount} mana of {@code color}, such as "{T}: Add {C}{C}{C}." */
    public static ActivatedAbility tapFor(ManaColor color, int amount) {
        return new ActivatedAbility(true, null,
                List.of(new AwardManaEffect(color, amount)),
                "{T}: Add " + ("{" + color.getCode() + "}").repeat(amount) + ".");
    }

    /**
     * The intrinsic "{T}: Add [mana]" ability of each basic land type (CR 305.6), one shared instance per color,
     * so the same ability is the same object wherever the engine lists it.
     */
    private static final Map<ManaColor, ActivatedAbility> INTRINSIC = intrinsicAbilities();

    private static Map<ManaColor, ActivatedAbility> intrinsicAbilities() {
        Map<ManaColor, ActivatedAbility> abilities = new EnumMap<>(ManaColor.class);
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN)) {
            abilities.put(color, tapFor(color));
        }
        return Collections.unmodifiableMap(abilities);
    }

    /** The intrinsic mana ability a basic land type that produces {@code color} gives a land (CR 305.6). */
    public static ActivatedAbility intrinsic(ManaColor color) {
        ActivatedAbility ability = INTRINSIC.get(color);
        if (ability == null) {
            throw new IllegalArgumentException("No basic land type produces " + color);
        }
        return ability;
    }

    /**
     * The intrinsic mana abilities a land card's own basic land types give it (CR 305.6), in the order of its
     * subtypes. A basic land has no other mana ability, so readers that look at a card rather than a permanent
     * add these to its activated abilities. Empty for a card that isn't a land.
     */
    public static List<ActivatedAbility> intrinsic(Card card) {
        if (!card.hasType(CardType.LAND) || card.getSubtypes() == null) {
            return List.of();
        }
        List<ActivatedAbility> abilities = new ArrayList<>();
        for (CardSubtype subtype : card.getSubtypes()) {
            ManaColor color = switch (subtype) {
                case PLAINS -> ManaColor.WHITE;
                case ISLAND -> ManaColor.BLUE;
                case SWAMP -> ManaColor.BLACK;
                case MOUNTAIN -> ManaColor.RED;
                case FOREST -> ManaColor.GREEN;
                default -> null;
            };
            if (color != null && !abilities.contains(INTRINSIC.get(color))) {
                abilities.add(INTRINSIC.get(color));
            }
        }
        return abilities;
    }

    /**
     * The activated abilities a card has on the battlefield: its own, then the intrinsic mana abilities of its basic
     * land types it doesn't print (CR 305.6). For readers that copy a card's abilities ("has all activated abilities
     * of …") rather than a permanent's.
     */
    public static List<ActivatedAbility> withIntrinsic(Card card) {
        List<ActivatedAbility> intrinsic = intrinsic(card);
        List<ActivatedAbility> printed = card.getActivatedAbilities(Zone.BATTLEFIELD);
        if (intrinsic.isEmpty()) {
            return printed;
        }
        List<ActivatedAbility> abilities = new ArrayList<>(printed);
        for (ActivatedAbility ability : intrinsic) {
            AwardManaEffect mana = (AwardManaEffect) ability.getEffects().getFirst();
            boolean printedAlready = printed.stream().anyMatch(other -> other.isRequiresTap()
                    && other.getEffects().size() == 1
                    && other.getEffects().getFirst() instanceof AwardManaEffect otherMana
                    && otherMana.color() == mana.color());
            if (!printedAlready) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    /** {@code {T}: Add one mana of any color.} */
    public static ActivatedAbility tapForAnyColor() {
        return new ActivatedAbility(true, null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color.");
    }
}
