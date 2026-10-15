package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "176")
@CardRegistration(set = "CMM", collectorNumber = "517")
@CardRegistration(set = "C14", collectorNumber = "27")
public class ObNixilisOfTheBlackOath extends Card {

    private static final String EMBLEM_TEXT =
            "{1}{B}, Sacrifice a creature: You gain X life and draw X cards, where X is the sacrificed creature's power.";

    public ObNixilisOfTheBlackOath() {
        // +2: Each opponent loses 1 life. You gain life equal to the life lost this way.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),
                "+2: Each opponent loses 1 life. You gain life equal to the life lost this way."
        ));

        // −2: Create a 5/5 black Demon creature token with flying. You lose 2 life.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new CreateTokenEffect("Demon", 5, 5, CardColor.BLACK,
                                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of()),
                        new LoseLifeEffect(2)
                ),
                "−2: Create a 5/5 black Demon creature token with flying. You lose 2 life."
        ));

        // −8: You get an emblem with "{1}{B}, Sacrifice a creature: You gain X life and draw X cards,
        // where X is the sacrificed creature's power."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemActivatedAbilityEffect(new ActivatedAbility(
                                false,
                                "{1}{B}",
                                List.of(
                                        new SacrificeCreatureCost(false, true),
                                        new GainLifeEffect(new XValue()),
                                        new DrawCardEffect(new XValue())
                                ),
                                EMBLEM_TEXT
                        ))),
                        EMBLEM_TEXT)),
                "−8: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
