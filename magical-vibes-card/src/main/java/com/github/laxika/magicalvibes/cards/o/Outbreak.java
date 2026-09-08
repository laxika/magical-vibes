package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "71")
public class Outbreak extends Card {

    public Outbreak() {
        addCastingOption(new AlternateHandCast(List.of(
                new DiscardCardCastingCost(new CardSubtypePredicate(CardSubtype.SWAMP), "a Swamp card"))));
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesOfChosenSubtypeEffect(-1, -1));
    }
}
