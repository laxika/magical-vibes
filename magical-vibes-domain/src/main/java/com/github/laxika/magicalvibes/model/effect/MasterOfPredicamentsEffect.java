package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Choose a card in your hand. [Guesser] guesses whether that card's mana value is greater than
 * [threshold]. If they guessed wrong, you may cast it without paying its mana cost. [If you don't
 * cast a spell this way, {@code ifNoSpellCast}.]"
 *
 * @param threshold     the number the guess is about, fixed as the effect resolves (4; the number of
 *                      artifacts you control for The Seventh Doctor)
 * @param guesser       the guessing player: the damaged player ({@code triggeringPlayer()}) or the
 *                      defending player
 * @param ifNoSpellCast the step resolved when no spell is cast this way (an empty hand, a right guess,
 *                      a land, a declined or impossible cast), or {@code null}
 */
public record MasterOfPredicamentsEffect(DynamicAmount threshold, PlayerSubject guesser, CardEffect ifNoSpellCast)
        implements CardEffect {

    /** Master of Predicaments: the damaged player guesses about 4. */
    public MasterOfPredicamentsEffect() {
        this(new Fixed(4), Subjects.triggeringPlayer(), null);
    }
}
