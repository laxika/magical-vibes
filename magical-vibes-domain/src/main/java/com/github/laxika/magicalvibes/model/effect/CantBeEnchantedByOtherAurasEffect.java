package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker restricting Aura attachment, or only Aura spell targeting when
 * {@code auraSpellsOnly} is true. A targeting-only restriction permits Auras to enter attached
 * without being cast, and permits attachment effects that do not target with an Aura spell.
 *
 * <p>By default an Aura already attached to the restricted permanent is put into its owner's
 * graveyard once the restriction applies (CR 704.5m). When {@code keepsExistingAuras} is true the
 * restriction only prevents new attachment, and Auras already attached stay attached (Guardian
 * Beast's "This effect doesn't remove Auras already attached to those artifacts").
 *
 * @param auraSpellsOnly whether only targeting by Aura spells is prohibited
 * @param keepsExistingAuras whether Auras already attached to the permanent are left in place
 */
public record CantBeEnchantedByOtherAurasEffect(boolean auraSpellsOnly, boolean keepsExistingAuras)
        implements CardEffect {

    public CantBeEnchantedByOtherAurasEffect() {
        this(false, false);
    }

    public CantBeEnchantedByOtherAurasEffect(boolean auraSpellsOnly) {
        this(auraSpellsOnly, false);
    }

    public static CantBeEnchantedByOtherAurasEffect keepingExistingAuras() {
        return new CantBeEnchantedByOtherAurasEffect(false, true);
    }
}
