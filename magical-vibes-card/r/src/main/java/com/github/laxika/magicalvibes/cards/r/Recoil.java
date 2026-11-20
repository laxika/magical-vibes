package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "INV", collectorNumber = "264")
@CardRegistration(set = "DDH", collectorNumber = "61")
@CardRegistration(set = "DMR", collectorNumber = "197")
public class Recoil extends Card {

    public Recoil() {
        target(new PermanentPredicateTargetFilter(new PermanentTruePredicate(),
                "Target must be a permanent"))
                .addSpellEffect(ReturnToHandEffect.target())
                        .addSpellEffect(new DiscardEffect(1, Subjects.ownerOf(Subjects.target())));
    }
}
