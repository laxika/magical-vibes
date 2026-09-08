package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "223")
public class MoritteOfTheFrost extends Card {

    public MoritteOfTheFrost() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentControlledBySourceControllerPredicate(),
                "permanent you control",
                Set.of(CardSupertype.LEGENDARY, CardSupertype.SNOW),
                Set.of(Keyword.CHANGELING),
                new Fixed(2),
                true
        ));
    }
}
