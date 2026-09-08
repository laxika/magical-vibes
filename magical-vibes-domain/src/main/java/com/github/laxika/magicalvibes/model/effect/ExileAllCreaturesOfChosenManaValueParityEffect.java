package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose odd or even. Exile each creature with mana value of the chosen quality." (Extinction
 * Event)
 *
 * <p>The choice is made while this spell resolves and is stored on {@code GameData} until the
 * effect resumes.</p>
 */
public record ExileAllCreaturesOfChosenManaValueParityEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
