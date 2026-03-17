package com.github.laxika.magicalvibes.cards.h;

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

@CardRegistration(set = "ISD", collectorNumber = "187")
public class HamletCaptain extends Card {

    public HamletCaptain() {
        // Whenever Hamlet Captain attacks or blocks, other Humans you control get +1/+1 until end of turn.
        var otherHumansFilter = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        var boostOtherHumans = new BoostAllOwnCreaturesEffect(1, 1, otherHumansFilter);
        addEffect(EffectSlot.ON_ATTACK, boostOtherHumans);
        addEffect(EffectSlot.ON_BLOCK, boostOtherHumans);
    }
}
