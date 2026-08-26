package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "62")
public class CabalTherapy extends Card {

    public CabalTherapy() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL,
                new ChooseNameRevealHandDiscardMatchingEffect(List.of(CardType.LAND)));
        addCastingOption(new FlashbackCast(List.of(
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate()))));
    }
}
