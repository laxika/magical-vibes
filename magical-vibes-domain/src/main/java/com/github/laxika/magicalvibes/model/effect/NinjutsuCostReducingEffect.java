package com.github.laxika.magicalvibes.model.effect;

/** Capability for static effects that reduce the generic mana portion of ninjutsu abilities. */
public interface NinjutsuCostReducingEffect extends CardEffect {

    /** Generic mana removed from a ninjutsu ability's activation cost. */
    int genericCostReduction();
}
