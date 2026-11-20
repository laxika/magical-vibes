package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "62")
public class ShatteredEgo extends Card {

    public ShatteredEgo() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-3, 0, Subjects.attachedCreature()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{U}",
                List.of(new PutTargetPermanentIntoLibraryNFromTopEffect(Subjects.attached(), 2)),
                "{3}{U}{U}: Put enchanted creature into its owner's library third from the top."
        ));
    }
}
