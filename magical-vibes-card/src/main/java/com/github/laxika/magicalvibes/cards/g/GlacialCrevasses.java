package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "187")
public class GlacialCrevasses extends Card {

    public GlacialCrevasses() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                                        new PermanentHasSupertypePredicate(CardSupertype.SNOW))),
                                "Sacrifice a snow Mountain", false),
                        PreventDamageEffect.allCombat()),
                "Sacrifice a snow Mountain: Prevent all combat damage that would be dealt this turn."));
    }
}
