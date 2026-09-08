package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "15")
public class Saltblast extends Card {

    public Saltblast() {
        PermanentPredicate nonwhitePermanent = new PermanentNotPredicate(
                new PermanentColorInPredicate(Set.of(CardColor.WHITE)));
        target(new PermanentPredicateTargetFilter(nonwhitePermanent, "Target must be a nonwhite permanent"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(nonwhitePermanent));
    }
}
