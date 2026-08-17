package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks an effect that needs a creature type chosen while its spell is being cast.
 */
public interface CastTimeCreatureTypeChoiceEffect extends CardEffect {

    boolean requiresCastTimeCreatureTypeChoice();
}
