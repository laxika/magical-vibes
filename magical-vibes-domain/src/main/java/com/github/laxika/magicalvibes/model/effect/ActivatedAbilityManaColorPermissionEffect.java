package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static self-only permission describing which mana colors may pay colored activation costs as
 * though they were mana of any color.
 */
public interface ActivatedAbilityManaColorPermissionEffect extends CardEffect {

    Set<ManaColor> manaColors();
}
