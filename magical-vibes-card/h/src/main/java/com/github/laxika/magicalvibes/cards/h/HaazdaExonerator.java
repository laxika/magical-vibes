package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "11")
public class HaazdaExonerator extends Card {

    public HaazdaExonerator() {
        // {T}, Sacrifice this creature: Destroy target Aura.
        var aura = new PermanentHasSubtypePredicate(CardSubtype.AURA);
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect(aura)),
                "{T}, Sacrifice Haazda Exonerator: Destroy target Aura.",
                new PermanentPredicateTargetFilter(aura, "Target must be an Aura")
        ));
    }
}
