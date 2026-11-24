package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "105")
@CardRegistration(set = "INR", collectorNumber = "475")
@CardRegistration(set = "UMA", collectorNumber = "104")
@CardRegistration(set = "MM3", collectorNumber = "76")
@CardRegistration(set = "DMU", collectorNumber = "97")
public class LilianaOfTheVeil extends Card {

    public LilianaOfTheVeil() {
        // +1: Each player discards a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DiscardEffect(1, Subjects.eachPlayer())),
                "+1: Each player discards a card."
        ));

        // −2: Target player sacrifices a creature.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SacrificePermanentsEffect(
                        1, new PermanentIsCreaturePredicate(), Subjects.targetPlayer())),
                "\u22122: Target player sacrifices a creature."
        ));

        // −6: Separate all permanents target player controls into two piles.
        //     That player sacrifices all permanents in the pile of their choice.
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(SeparateIntoPilesEffect.permanents(
                                Subjects.controlledBy(Subjects.targetPlayer(), new PermanentTruePredicate()), "permanents"),
                        new SacrificeSelfEffect(Subjects.thisWay(StepKind.CHOOSE), Subjects.targetPlayer())),
                "\u22126: Separate all permanents target player controls into two piles. That player sacrifices all permanents in the pile of their choice."
        ));
    }
}
