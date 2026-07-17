package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "275")
@CardRegistration(set = "5ED", collectorNumber = "354")
public class BottleOfSuleiman extends Card {

    public BottleOfSuleiman() {
        // {1}, Sacrifice this artifact: Flip a coin. If you win, create a 5/5 colorless
        // Djinn artifact creature token with flying. If you lose, this deals 5 damage to you.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new FlipCoinWinEffect(
                                new CreateTokenEffect("Djinn", 5, 5, null,
                                        List.of(CardSubtype.DJINN), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)),
                                new DealDamageToPlayersEffect(5, DamageRecipient.CONTROLLER))
                ),
                "{1}, Sacrifice Bottle of Suleiman: Flip a coin. If you win the flip, create a 5/5 colorless "
                        + "Djinn artifact creature token with flying. If you lose the flip, Bottle of Suleiman deals 5 damage to you."
        ));
    }
}
