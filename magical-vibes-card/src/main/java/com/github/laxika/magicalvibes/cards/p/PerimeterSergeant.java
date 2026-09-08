package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "27")
public class PerimeterSergeant extends Card {

    public PerimeterSergeant() {
        var otherHumansFilter = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 0, otherHumansFilter));
    }
}
