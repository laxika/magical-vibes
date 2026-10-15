package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "178")
public class PetAvengers extends Card {

    public PetAvengers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{G}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect("Hero", 3, 2, CardColor.WHITE, List.of(CardSubtype.HERO),
                                Set.of(Keyword.VIGILANCE), Set.of())
                ),
                "Power-up — {6}{G}: Put a +1/+1 counter on this creature and create a 3/2 white Hero creature token with vigilance. "
                        + "Activate each power-up ability only once. Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
