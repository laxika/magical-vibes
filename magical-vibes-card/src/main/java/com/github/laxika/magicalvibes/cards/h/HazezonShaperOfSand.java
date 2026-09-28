package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "229")
@CardRegistration(set = "DMC", collectorNumber = "32")
@CardRegistration(set = "DMC", collectorNumber = "54")
public class HazezonShaperOfSand extends Card {

    public HazezonShaperOfSand() {
        addEffect(EffectSlot.STATIC,
                new PlayLandsFromGraveyardEffect(new CardSubtypePredicate(CardSubtype.DESERT)));

        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DESERT),
                        new CreateTokenEffect(2, "Sand Warrior", 1, 1,
                                CardColor.RED,
                                Set.of(CardColor.RED, CardColor.GREEN, CardColor.WHITE),
                                List.of(CardSubtype.WARRIOR))));
    }
}
