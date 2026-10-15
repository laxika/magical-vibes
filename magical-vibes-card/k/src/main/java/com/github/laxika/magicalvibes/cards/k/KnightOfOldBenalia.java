package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "17")
public class KnightOfOldBenalia extends Card {

    public KnightOfOldBenalia() {
        // When this creature enters, other creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(
                1,
                1,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(),
                "Suspend 5—{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(5));
    }
}
