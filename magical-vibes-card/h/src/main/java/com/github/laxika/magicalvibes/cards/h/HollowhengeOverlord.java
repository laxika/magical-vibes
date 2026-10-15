package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOC", collectorNumber = "36")
@CardRegistration(set = "VOC", collectorNumber = "74")
public class HollowhengeOverlord extends Card {

    public HollowhengeOverlord() {
        PermanentPredicate wolfOrWerewolf = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                new PermanentCount(wolfOrWerewolf, CountScope.CONTROLLER),
                "Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF),
                Set.of(), Set.of()));
    }
}
