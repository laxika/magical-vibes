package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "204")
public class MagistratesVeto extends Card {

    public MagistratesVeto() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)))),
                new PermanentTruePredicate(),
                "White creatures and blue creatures can't block"));
    }
}
