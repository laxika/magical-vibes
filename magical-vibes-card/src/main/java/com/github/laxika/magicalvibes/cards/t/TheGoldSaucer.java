package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "279")
public class TheGoldSaucer extends Card {

    public TheGoldSaucer() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new FlipCoinWinEffect(CreateTokenEffect.ofTreasureToken(1))),
                "{2}, {T}: Flip a coin. If you win the flip, create a Treasure token."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new DrawCardEffect(1)
                ),
                "{3}, Sacrifice two artifacts: Draw a card."
        ));
    }
}
