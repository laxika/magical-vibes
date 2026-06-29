package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "143")
public class StromkirkCaptain extends Card {

    public StromkirkCaptain() {
        // Other Vampire creatures you control get +1/+1 and have first strike.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.FIRST_STRIKE), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VAMPIRE))));
    }
}
