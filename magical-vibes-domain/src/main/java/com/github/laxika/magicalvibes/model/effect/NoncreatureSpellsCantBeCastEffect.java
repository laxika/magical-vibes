package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect preventing noncreature spells whose mana value is {@code minManaValue} or greater
 * from being cast. When {@code restrictXSpells} is true, noncreature spells with {@code {X}} in
 * their mana costs are also restricted. The default constructor is global and symmetric, while
 * the three-argument form can restrict only the source permanent's controller.
 */
public record NoncreatureSpellsCantBeCastEffect(int minManaValue, boolean restrictXSpells,
                                                boolean appliesToAllPlayers) implements CardEffect {

    public NoncreatureSpellsCantBeCastEffect(int minManaValue, boolean restrictXSpells) {
        this(minManaValue, restrictXSpells, true);
    }
}
