package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEachControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldOrElseEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "3")
@CardRegistration(set = "LCC", collectorNumber = "19")
@CardRegistration(set = "LCC", collectorNumber = "29")
@CardRegistration(set = "LCC", collectorNumber = "123")
public class HakbalOfTheSurgingSoul extends Card {

    public HakbalOfTheSurgingSoul() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ExploreEachControlledCreatureEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)))));

        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new PutCardToBattlefieldOrElseEffect(land, "land", new DrawCardEffect()),
                "Put a land card from your hand onto the battlefield?",
                new DrawCardEffect()));
    }
}
