package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "122")
public class RuinInTheirWake extends Card {

    public RuinInTheirWake() {
        addEffect(EffectSlot.SPELL, new SearchLibraryAndConditionalEffect(
                CardPredicateUtils.basicLand(),
                LibrarySearchDestination.HAND,
                CardPredicateUtils.basicLand(),
                new ConditionalEffect(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNamedPredicate("Wastes")))),
                        MayPutSelectedCardOntoBattlefieldEffect.forCard(
                                CardPredicateUtils.basicLand(), true))));
    }
}
