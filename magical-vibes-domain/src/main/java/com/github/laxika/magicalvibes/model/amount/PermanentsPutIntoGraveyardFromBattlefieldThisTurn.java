package com.github.laxika.magicalvibes.model.amount;

/**
 * Counts the permanents put into graveyards from the battlefield this turn, every player's (gravestorm, CR 702.69a:
 * "copy it for each permanent that was put into a graveyard from the battlefield this turn").
 */
public record PermanentsPutIntoGraveyardFromBattlefieldThisTurn() implements DynamicAmount {
}
