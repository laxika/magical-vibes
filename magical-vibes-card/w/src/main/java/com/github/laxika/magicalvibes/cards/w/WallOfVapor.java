package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "27")
@CardRegistration(set = "LEG", collectorNumber = "84")
public class WallOfVapor extends Card {

    public WallOfVapor() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.source())
                .from(new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentBlockedBySourcePredicate()))));
    }
}
