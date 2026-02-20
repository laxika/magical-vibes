package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

import java.util.UUID;

public record ReplaceSingleDrawEffect(UUID playerId, DrawReplacementKind kind) implements CardEffect {
}
