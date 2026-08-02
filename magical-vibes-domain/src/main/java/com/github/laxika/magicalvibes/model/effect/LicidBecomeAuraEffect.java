package com.github.laxika.magicalvibes.model.effect;

/**
 * The Licid activated ability: "This creature loses this ability and becomes an Aura enchantment
 * with enchant creature. Attach it to target creature. You may pay {@code endCost} to end this
 * effect."
 *
 * <p>Resolution replaces the source permanent's card with a runtime copy that is an Aura
 * enchantment (no longer a creature, no power/toughness, no Licid ability) carrying a single
 * {@code endCost} activated ability backed by {@link LicidEndEffect}, then attaches it to the
 * targeted creature. The permanent's STATIC effects ride along on the copy, so an
 * {@code ENCHANTED_CREATURE}-scoped grant only starts applying once the Licid is attached.</p>
 *
 * <p>If the target is illegal on resolution the whole ability is countered, so the Licid stays a
 * creature (Tempest FAQ) — the handler simply does nothing.</p>
 *
 * @param endCost the mana cost the controller may pay to turn the Licid back into a creature
 */
public record LicidBecomeAuraEffect(String endCost) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
