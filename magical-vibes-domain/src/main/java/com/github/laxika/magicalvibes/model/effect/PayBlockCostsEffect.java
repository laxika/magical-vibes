package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import java.util.List;

/** Retains proposed blocks while the defending player decides whether to pay their costs. */
public record PayBlockCostsEffect(PendingInteraction.BlockerDeclaration declaration,
                                 List<Integer> blockerIndices,
                                 List<Integer> attackerIndices,
                                 String declaringPlayerName) implements CardEffect {
    public PayBlockCostsEffect {
        blockerIndices = List.copyOf(blockerIndices);
        attackerIndices = List.copyOf(attackerIndices);
    }
}
