package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a creature type, then destroys every creature that has none of the chosen
 * types. The destruction does not allow regeneration.
 */
public record DestroyAllCreaturesExceptChosenTypesEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
