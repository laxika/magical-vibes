package com.github.laxika.magicalvibes.model.effect;

/**
 * "Draw a card if its power is N or greater. Otherwise, [effect]" (Tribute to the World Tree): chooses one of two
 * effects from the power of the stack entry's triggering permanent as the ability resolves, else as it last existed
 * on the battlefield (CR 608.2h). A branch that affects "it" reads the triggering permanent too
 * ({@code PutCounterOnTargetPermanentEffect.onTriggeringPermanent}).
 */
public record EnteringCreaturePowerBranchEffect(
        int minPower,
        CardEffect powerAtLeast,
        CardEffect belowPower
) implements CardEffect {
}
