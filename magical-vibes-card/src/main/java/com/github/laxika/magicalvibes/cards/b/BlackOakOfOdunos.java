package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "62")
public class BlackOakOfOdunos extends Card {

    public BlackOakOfOdunos() {
        addActivatedAbility(new ActivatedAbility(
                false, "{B}",
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, false),
                        new BoostSelfEffect(1, 1)
                ),
                "{B}, Tap another untapped creature you control: This creature gets +1/+1 until end of turn."
        ));
    }
}
