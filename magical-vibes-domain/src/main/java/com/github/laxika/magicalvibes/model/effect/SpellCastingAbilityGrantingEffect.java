package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** A static effect that grants a spell-casting ability to matching spells. */
public interface SpellCastingAbilityGrantingEffect extends CardEffect {

    CardPredicate filter();

    Keyword grantedAbility();

    /** Restricts the grant to spells cast from one zone; null means every source zone. */
    default Zone sourceZone() {
        return null;
    }

    default boolean appliesToSourceZone(Zone sourceZone) {
        return sourceZone() == null || sourceZone() == sourceZone;
    }
}
