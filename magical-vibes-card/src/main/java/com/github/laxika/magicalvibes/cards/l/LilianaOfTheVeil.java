package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SeparatePermanentsIntoPilesAndSacrificeEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "105")
public class LilianaOfTheVeil extends Card {

    public LilianaOfTheVeil() {
        // +1: Each player discards a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new EachPlayerDiscardsEffect(1)),
                "+1: Each player discards a card."
        ));

        // −2: Target player sacrifices a creature.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SacrificeCreatureEffect()),
                "\u22122: Target player sacrifices a creature."
        ));

        // −6: Separate all permanents target player controls into two piles.
        //     That player sacrifices all permanents in the pile of their choice.
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new SeparatePermanentsIntoPilesAndSacrificeEffect()),
                "\u22126: Separate all permanents target player controls into two piles. That player sacrifices all permanents in the pile of their choice."
        ));
    }
}
