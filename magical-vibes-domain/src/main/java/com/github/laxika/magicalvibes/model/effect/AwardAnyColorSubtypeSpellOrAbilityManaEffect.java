package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Produces {@code amount} mana in any combination of colors (player chooses one color per mana)
 * that can only be spent to cast spells of the given creature {@code subtype} or to activate
 * abilities of permanents of that subtype.
 * (e.g. Smokebraider: "{T}: Add two mana in any combination of colors. Spend this mana only to
 *  cast Elemental spells or activate abilities of Elementals.")
 *
 * <p>Routed into {@link com.github.laxika.magicalvibes.model.ManaPool}'s per-subtype/per-color
 * spell-or-ability bucket. Distinct from {@link AwardAnyColorChosenSubtypeCreatureManaEffect}
 * (Pillar of Origins / Unclaimed Territory), whose mana is spell-only and therefore cannot pay
 * activated abilities.
 */
public record AwardAnyColorSubtypeSpellOrAbilityManaEffect(int amount, CardSubtype subtype) implements ManaProducingEffect {
}
