package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.RevealUntilColorToHandRestExiledEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "38")
public class SacredGuide extends Card {

    public SacredGuide() {
        // {1}{W}, Sacrifice this creature: Reveal cards from the top of your library until you
        // reveal a white card. Put that card into your hand and exile all other cards revealed
        // this way.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{W}",
                List.of(new SacrificeSelfCost(),
                        new RevealUntilColorToHandRestExiledEffect(CardColor.WHITE)),
                "{1}{W}, Sacrifice this creature: Reveal cards from the top of your library until "
                        + "you reveal a white card. Put that card into your hand and exile all other "
                        + "cards revealed this way."
        ));
    }
}
