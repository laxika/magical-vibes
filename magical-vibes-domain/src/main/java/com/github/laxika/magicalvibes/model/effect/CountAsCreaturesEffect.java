package com.github.laxika.magicalvibes.model.effect;

/**
 * Static characteristic rule that makes the source count as the given number of creatures for
 * effects that count creatures. The source remains one permanent and is still only one creature
 * for effects counting other characteristics.
 */
public record CountAsCreaturesEffect(int count) implements CardEffect {
}
