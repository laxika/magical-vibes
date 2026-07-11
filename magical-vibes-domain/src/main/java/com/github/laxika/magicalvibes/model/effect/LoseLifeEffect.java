package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes one or more players lose life. A single record covers the whole life-loss family: the
 * {@link LoseLifeRecipient} routes who loses life (controller / target player / each player / each
 * opponent) and the {@link DynamicAmount} covers fixed amounts ("loses 3 life"), an X value
 * (Exsanguinate), or a relational amount such as {@code PermanentCount} ("1 life for each Vampire
 * you control", Bishop of the Bloodstained) or {@code EventValue} ("equal to the life you gained",
 * Sanguine Bond). When {@code controllerGainsLifeLost} is set the controller then gains life equal
 * to the total life lost this way (the "each opponent loses N life, you gain that much" drain
 * pattern — Blood Tithe, Chancellor of the Dross, Exsanguinate).
 *
 * <p>Life loss is <em>not</em> damage (CR 118.2): it is applied through the shared life-loss
 * primitive and feeds "loses life" triggers, never through damage plumbing.
 *
 * @param amount                  amount of life to lose
 * @param recipient               who loses life
 * @param controllerGainsLifeLost when {@code true} the controller gains the total life lost
 */
public record LoseLifeEffect(DynamicAmount amount, LoseLifeRecipient recipient,
                             boolean controllerGainsLifeLost) implements CardEffect {

    /** Fixed amount, with drain flag. */
    public LoseLifeEffect(int amount, LoseLifeRecipient recipient, boolean controllerGainsLifeLost) {
        this(new Fixed(amount), recipient, controllerGainsLifeLost);
    }

    /** Dynamic amount, non-drain. */
    public LoseLifeEffect(DynamicAmount amount, LoseLifeRecipient recipient) {
        this(amount, recipient, false);
    }

    /** Fixed amount, non-drain. */
    public LoseLifeEffect(int amount, LoseLifeRecipient recipient) {
        this(new Fixed(amount), recipient, false);
    }

    /** Fixed amount, controller loses life, non-drain (self life-loss cost/drawback). */
    public LoseLifeEffect(int amount) {
        this(new Fixed(amount), LoseLifeRecipient.CONTROLLER, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return recipient == LoseLifeRecipient.TARGET_PLAYER;
    }
}
