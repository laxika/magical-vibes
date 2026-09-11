package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "120")
public class PelakkaPredation extends Card {

    public PelakkaPredation() {
        setBackFaceCard(new PelakkaCaverns());
        setModalDoubleFaced(true);

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                1, List.of(), new CardMinManaValuePredicate(3), HandChoiceDestination.DISCARD));
    }

    @Override
    public String getBackFaceClassName() {
        return "PelakkaCaverns";
    }
}
