package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "279")
public class Aurochs extends Card {

    public Aurochs() {
        // Trample is auto-loaded from Scryfall.
        // Whenever Aurochs attacks, it gets +1/+0 until end of turn
        // for each other attacking Aurochs.
        PermanentCount otherAttackingAurochs = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.AUROCHS)
                )),
                CountScope.CONTROLLER,
                true);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(otherAttackingAurochs, new Fixed(0)));
    }
}
