package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "26")
public class SunscourgeChampion extends Card {

    public SunscourgeChampion() {
        // When this creature enters, you gain life equal to its power.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new SourcePower()));

        // Eternalize—{2}{W}{W}, Discard a card. ({2}{W}{W}, Discard a card, Exile this card from your
        // graveyard: Create a token that's a copy of it, except it's a 4/4 black Zombie Human Wizard with
        // no mana cost. Eternalize only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4)
                ),
                "Eternalize—{2}{W}{W}, Discard a card. ({2}{W}{W}, Discard a card, Exile this card from your "
                        + "graveyard: Create a token that's a copy of it, except it's a 4/4 black Zombie Human Wizard "
                        + "with no mana cost. Eternalize only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
