package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "133")
public class IndenturedOaf extends Card {

    public IndenturedOaf() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.RED))))))
                .from(new PermanentIsSourcePermanentPredicate()));
    }
}
