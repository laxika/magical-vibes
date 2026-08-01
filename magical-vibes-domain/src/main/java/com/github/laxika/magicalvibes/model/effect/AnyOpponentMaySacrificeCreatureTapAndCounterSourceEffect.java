package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Punisher trigger (Desecration Demon): any opponent may sacrifice a creature of their choice. If a
 * player does, tap the source and put a +1/+1 counter on it.
 *
 * <p>Every opponent gets the choice, in turn order, even after an earlier opponent already
 * sacrificed; opponents controlling no creature are skipped without a prompt. The tap and the single
 * +1/+1 counter happen once after every opponent has chosen, no matter how many creatures were
 * sacrificed. The no-arg constructor is the one used in card definitions; resolution stamps the
 * remaining-opponent queue and the source ids onto the instances carried by the may prompts.
 *
 * @param remainingOpponentIds opponents still to choose (null in the card definition)
 * @param abilityControllerId  controller of the triggered ability (null in the card definition)
 * @param sourcePermanentId    the permanent to tap and put the counter on (null in the card definition)
 * @param anyAccepted          whether any opponent has already sacrificed during this resolution
 */
public record AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        boolean anyAccepted
) implements CardEffect {

    public AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect() {
        this(null, null, null, false);
    }
}
