package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "261")
public class SpringleafDrum extends Card {

    public SpringleafDrum() {
        // {T}, Tap an untapped creature you control: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate()),
                        new AwardAnyColorManaEffect()),
                "{T}, Tap an untapped creature you control: Add one mana of any color."
        ));
    }
}
