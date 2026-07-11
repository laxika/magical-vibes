package com.github.laxika.magicalvibes.model.effect;

/**
 * Soulbright Flamekin's activated-ability resolution: if this is the third time the ability has
 * resolved this turn, the controller adds {@code {R}{R}{R}{R}{R}{R}{R}{R}}.
 *
 * <p>The unconditional "target creature gains trample until end of turn" half of the ability is a
 * plain {@link GrantKeywordEffect}; this effect handles only the conditional mana burst. Counting is
 * per source permanent and by resolutions (via {@code GameData.permanentAbilityResolutionsThisTurn},
 * reset each turn), so the mana is produced only on the exact third resolution and not on any later
 * one — CR-consistent with the card's rulings. The printed "you may" is a no-op here: the engine has
 * no mana burn, so declining the mana would never differ from adding it.
 */
public record SoulbrightFlamekinEffect() implements CardEffect {
}
