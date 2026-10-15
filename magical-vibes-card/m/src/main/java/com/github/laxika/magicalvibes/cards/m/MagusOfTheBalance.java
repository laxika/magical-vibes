package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsDownToFewestEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToFewestEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "5")
public class MagusOfTheBalance extends Card {

    public MagusOfTheBalance() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new EachPlayerSacrificesDownToFewestEffect(new PermanentIsLandPredicate()),
                        new EachPlayerDiscardsDownToFewestEffect(),
                        new EachPlayerSacrificesDownToFewestEffect(new PermanentIsCreaturePredicate())
                ),
                "{4}{W}, {T}, Sacrifice this creature: Each player chooses a number of lands they control "
                        + "equal to the number of lands controlled by the player who controls the fewest, "
                        + "then sacrifices the rest. Players discard cards and sacrifice creatures the same way."
        ));
    }
}
