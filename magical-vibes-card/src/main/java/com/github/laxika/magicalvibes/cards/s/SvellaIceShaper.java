package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AetherworksMarvelEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "230")
public class SvellaIceShaper extends Card {

    public SvellaIceShaper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(CreateTokenEffect.ofSnowArtifactToken(
                        1,
                        "Icy Manalith",
                        List.of(),
                        List.of(new ActivatedAbility(
                                true,
                                null,
                                List.of(new AwardAnyColorManaEffect()),
                                "{T}: Add one mana of any color.")))),
                "{3}, {T}: Create a colorless snow artifact token named Icy Manalith with "
                        + "\"{T}: Add one mana of any color.\""));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}{R}{G}",
                List.of(new AetherworksMarvelEffect(4)),
                "{6}{R}{G}, {T}: Look at the top four cards of your library. You may cast a spell "
                        + "from among them without paying its mana cost. Put the rest on the bottom "
                        + "of your library in a random order."));
    }
}
