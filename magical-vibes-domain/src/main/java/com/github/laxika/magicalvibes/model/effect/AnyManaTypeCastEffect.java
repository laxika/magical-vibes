package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Capability interface for static effects that let the controller spend mana of any type to cast
 * spells of certain card types (e.g. Vizier of the Menagerie: "You can spend mana of any type to
 * cast creature spells."). Colored mana requirements of a matching spell may be paid with mana of
 * any type, including colorless.
 *
 * <p>Read by {@code CastingPermissionService} on both the payment side and the view playability
 * side, so a matching spell is affordable and paid as if its whole cost were generic.
 */
public interface AnyManaTypeCastEffect extends CardEffect {

    /** Card types whose spells this permanent lets the controller cast with mana of any type. */
    Set<CardType> spellTypes();
}
