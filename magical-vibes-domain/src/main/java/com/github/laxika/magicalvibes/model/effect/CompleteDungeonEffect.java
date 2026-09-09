package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Dungeon;

/** Completes the specified dungeon after its final room ability resolves. */
public record CompleteDungeonEffect(Dungeon dungeon) implements CardEffect {
}
