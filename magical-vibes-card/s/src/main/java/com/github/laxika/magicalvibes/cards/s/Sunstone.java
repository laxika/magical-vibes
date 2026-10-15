package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "341")
public class Sunstone extends Card {

    public Sunstone() {
        // {2}, Sacrifice a snow land: Prevent all combat damage that would be dealt this turn.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.SNOW)
                                )),
                                "Sacrifice a snow land", false),
                        PreventDamageEffect.allCombat()),
                "{2}, Sacrifice a snow land: Prevent all combat damage that would be dealt this turn."));
    }
}
