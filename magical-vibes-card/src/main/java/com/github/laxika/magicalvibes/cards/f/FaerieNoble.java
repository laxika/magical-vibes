package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "86")
public class FaerieNoble extends Card {

    public FaerieNoble() {
        var faerie = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FAERIE));

        // Other Faerie creatures you control get +0/+1. (OWN_CREATURES already excludes this creature.)
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, Set.of(), GrantScope.OWN_CREATURES, faerie));

        // {T}: Other Faerie creatures you control get +1/+0 until end of turn.
        var otherFaeries = new PermanentAllOfPredicate(
                List.of(faerie, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new BoostAllOwnCreaturesEffect(1, 0, otherFaeries)),
                "{T}: Other Faerie creatures you control get +1/+0 until end of turn."));
    }
}
