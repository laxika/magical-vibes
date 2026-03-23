package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "81")
public class ChancellorOfTheForge extends Card {

    public ChancellorOfTheForge() {
        // You may reveal this card from your opening hand. If you do, at the beginning of
        // the first upkeep, create a 1/1 red Phyrexian Goblin creature token with haste.
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new CreateTokenEffect(
                        "Phyrexian Goblin", 1, 1, CardColor.RED,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN),
                        Set.of(Keyword.HASTE), Set.of()),
                "Reveal this card from your opening hand?"
        ));

        // When this creature enters, create X 1/1 red Phyrexian Goblin creature tokens with haste,
        // where X is the number of creatures you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokensEqualToControlledCreatureCountEffect(
                "Phyrexian Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN),
                Set.of(Keyword.HASTE), Set.of()));
    }
}
