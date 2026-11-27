package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Each opponent exiles a nonland, nontoken permanent they control and a random nonland card from their library, then
 * [the source] deals damage to them equal to the total mana value of the cards exiled this way" (Gilded Ambusher),
 * after each opponent has chosen their permanent ({@code ChoosePermanentsEffect.eachChoosesOne}, CR 101.4): the chosen
 * permanents are exiled together, then each opponent's random nonland library card, then each opponent is dealt the
 * total mana value of their own exiled cards.
 *
 * @param chosen the permanents the opponents chose ({@code Subjects.thisWay(StepKind.CHOOSE)})
 */
public record EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect(
        PermanentSubject chosen
) implements CardEffect {

    public EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect {
        Objects.requireNonNull(chosen, "chosen");
        if (chosen.declaresTarget()) {
            throw new IllegalArgumentException("The opponents choose the permanents; none is targeted: " + chosen);
        }
    }
}
