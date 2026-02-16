package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public sealed interface PermanentChoiceContext {

    record CloneCopy(UUID clonePermanentId) implements PermanentChoiceContext {}

    record AuraGraft(UUID auraPermanentId) implements PermanentChoiceContext {}

    record LegendRule(String cardName) implements PermanentChoiceContext {}

    record BounceCreature(UUID bouncingPlayerId) implements PermanentChoiceContext {}

    record CopySpellRetarget(UUID copyCardId) implements PermanentChoiceContext {}
}
