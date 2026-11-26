package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "114")
public class FodderLaunch extends Card {

    public FodderLaunch() {
        addCastingCost(new SacrificePermanentCost(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                "Sacrifice a Goblin"
        ));
        addSpellEffect(BoostEffect.target(-5, -5));
        addSpellEffect(DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 5));
    }
}
