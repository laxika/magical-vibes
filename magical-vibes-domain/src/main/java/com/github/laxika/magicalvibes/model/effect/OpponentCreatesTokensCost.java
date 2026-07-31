package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect paid by having an opponent create {@code count} tokens (Varchild's War-Riders'
 * cumulative upkeep — "Have an opponent create a 1/1 red Survivor creature token"). The tokens
 * enter under the opponent's control, so the payment costs the source's controller nothing but
 * board position. Always payable: there is no resource the payer can run out of.
 *
 * @param count how many tokens the opponent creates (one per age counter for cumulative upkeep)
 * @param tokenTemplate blueprint for a single token; its own {@code amount} is ignored
 */
public record OpponentCreatesTokensCost(int count, CreateTokenEffect tokenTemplate) implements CostEffect {

    public OpponentCreatesTokensCost {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("tokenTemplate must not be null");
        }
    }
}
