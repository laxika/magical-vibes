package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may pay any amount of mana; then each player creates token copies based on the
 * payments. By default, each player creates a number of copies equal to the amount they paid;
 * {@link #eachPlayerCreatesTotalMana(CreateTokenEffect)} makes every player create a number equal
 * to the total amount paid by all players.
 * <p>
 * Single pass, not round-robin: every player is prompted exactly once, in APNAP order (CR 101.4 —
 * the active player chooses first, then the remaining players in turn order). Each choice is an
 * X-value mana-payment prompt capped by that player's potential mana, so a player may tap sources
 * while the prompt is open. Used by Liege of the Hollows and Alliance of Arms.
 *
 * @param token a single-token template
 * @param eachPlayerCreatesTotal whether every player creates the total amount paid by all players
 */
public record EachPlayerPaysAnyManaForTokensEffect(CreateTokenEffect token, boolean eachPlayerCreatesTotal)
        implements CardEffect {

    public EachPlayerPaysAnyManaForTokensEffect(CreateTokenEffect token) {
        this(token, false);
    }

    public static EachPlayerPaysAnyManaForTokensEffect eachPlayerCreatesTotalMana(CreateTokenEffect token) {
        return new EachPlayerPaysAnyManaForTokensEffect(token, true);
    }
}
