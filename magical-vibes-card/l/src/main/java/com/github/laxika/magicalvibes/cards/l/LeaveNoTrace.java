package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "23")
public class LeaveNoTrace extends Card {

    public LeaveNoTrace() {
        target(TargetFilters.enchantment())
                .addSpellEffect(new DestroyTargetPermanentEffect(Subjects.allOf(Subjects.target(),
                        Subjects.otherPermanentsSharing(Subjects.target(), SharedQuality.COLOR,
                                new PermanentIsEnchantmentPredicate()))));
    }
}
