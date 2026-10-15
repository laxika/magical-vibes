package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "45")
public class SinuousStriker extends Card {

    public SinuousStriker() {
        // {U}: This creature gets +1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new BoostSelfEffect(1, -1)),
                "{U}: This creature gets +1/-1 until end of turn."));

        // Eternalize—{3}{U}{U}, Discard a card. ({3}{U}{U}, Discard a card, Exile this card from your
        // graveyard: Create a token that's a copy of it, except it's a 4/4 black Zombie Snake Warrior with
        // no mana cost. Eternalize only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4)
                ),
                "Eternalize—{3}{U}{U}, Discard a card. ({3}{U}{U}, Discard a card, Exile this card from your "
                        + "graveyard: Create a token that's a copy of it, except it's a 4/4 black Zombie Snake Warrior "
                        + "with no mana cost. Eternalize only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
