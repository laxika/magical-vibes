package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfChosenTypeToHandRestEffect;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "193")
public class WindingWay extends Card {

    public WindingWay() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsOfChosenTypeToHandRestEffect(
                4, LookDestination.GRAVEYARD, List.of(CardType.CREATURE, CardType.LAND)));
    }
}
