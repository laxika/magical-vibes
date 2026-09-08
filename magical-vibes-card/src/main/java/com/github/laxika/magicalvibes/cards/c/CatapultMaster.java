package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "10")
public class CatapultMaster extends Card {

    public CatapultMaster() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(5, new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)),
                        new ExileTargetPermanentEffect()),
                "Tap five untapped Soldiers you control: Exile target creature.",
                TargetFilters.creature()
        ));
    }
}
